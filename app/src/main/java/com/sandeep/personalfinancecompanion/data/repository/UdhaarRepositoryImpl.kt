package com.sandeep.personalfinancecompanion.data.repository

import com.sandeep.personalfinancecompanion.data.local.dao.UdhaarDao
import com.sandeep.personalfinancecompanion.data.mapper.toDomain
import com.sandeep.personalfinancecompanion.data.mapper.toEntity
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntry
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntryType
import com.sandeep.personalfinancecompanion.domain.model.UdhaarOverview
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPerson
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPersonSummary
import com.sandeep.personalfinancecompanion.domain.repository.UdhaarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class UdhaarRepositoryImpl @Inject constructor(
    private val dao: UdhaarDao
) : UdhaarRepository {

    override fun getPeople(): Flow<List<UdhaarPerson>> {
        return dao.getPeople().map { people -> people.map { it.toDomain() } }
    }

    override fun getSummaries(): Flow<List<UdhaarPersonSummary>> {
        return combine(dao.getPeople(), dao.getEntries()) { peopleEntities, entryEntities ->
            val people = peopleEntities.map { it.toDomain() }
            val entries = entryEntities.map { it.toDomain() }

            people.mapNotNull { person ->
                val history = entries
                    .filter { it.personId == person.id }
                    .sortedByDescending { it.date }
                val net = history.sumOf { it.signedAmount() }

                if (history.isEmpty() && person.phoneNumber.isNullOrBlank()) {
                    null
                } else {
                    UdhaarPersonSummary(
                        person = person,
                        netAmount = abs(net),
                        isOwedToYou = net >= 0,
                        history = history
                    )
                }
            }.filter { it.history.isNotEmpty() || it.person.phoneNumber?.isNotBlank() == true }
                .sortedWith(
                    compareByDescending<UdhaarPersonSummary> { it.netAmount }
                        .thenBy { it.person.name.lowercase() }
                )
        }
    }

    override fun getOverview(): Flow<UdhaarOverview> {
        return getSummaries().map { summaries ->
            UdhaarOverview(
                totalReceivable = summaries.filter { it.isOwedToYou }.sumOf { it.netAmount },
                totalPayable = summaries.filter { !it.isOwedToYou }.sumOf { it.netAmount },
                activePeople = summaries.count { it.netAmount > 0.0 }
            )
        }
    }

    override suspend fun savePerson(person: UdhaarPerson) {
        withContext(Dispatchers.IO) {
            dao.upsertPerson(person.toEntity())
        }
    }

    override suspend fun addEntry(entry: UdhaarEntry) {
        withContext(Dispatchers.IO) {
            dao.insertEntry(entry.toEntity())
        }
    }

    override suspend fun deleteEntry(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteEntry(id)
        }
    }

    private fun UdhaarEntry.signedAmount(): Double {
        return when (type) {
            UdhaarEntryType.GIVEN -> amount
            UdhaarEntryType.PAID_BACK -> amount
            UdhaarEntryType.TAKEN -> -amount
            UdhaarEntryType.RECEIVED_BACK -> -amount
        }
    }
}
