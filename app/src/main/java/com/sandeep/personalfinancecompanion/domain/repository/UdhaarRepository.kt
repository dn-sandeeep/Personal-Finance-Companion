package com.sandeep.personalfinancecompanion.domain.repository

import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntry
import com.sandeep.personalfinancecompanion.domain.model.UdhaarOverview
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPerson
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPersonSummary
import kotlinx.coroutines.flow.Flow

interface UdhaarRepository {
    fun getPeople(): Flow<List<UdhaarPerson>>
    fun getSummaries(): Flow<List<UdhaarPersonSummary>>
    fun getOverview(): Flow<UdhaarOverview>
    suspend fun savePerson(person: UdhaarPerson)
    suspend fun deletePerson(id: String)
    suspend fun addEntry(entry: UdhaarEntry)
    suspend fun deleteEntry(id: String)
}
