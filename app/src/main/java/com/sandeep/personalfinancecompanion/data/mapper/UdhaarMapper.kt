package com.sandeep.personalfinancecompanion.data.mapper

import com.sandeep.personalfinancecompanion.data.local.entity.UdhaarEntryEntity
import com.sandeep.personalfinancecompanion.data.local.entity.UdhaarPersonEntity
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntry
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntryType
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPerson

fun UdhaarPersonEntity.toDomain(): UdhaarPerson {
    return UdhaarPerson(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UdhaarPerson.toEntity(): UdhaarPersonEntity {
    return UdhaarPersonEntity(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UdhaarEntryEntity.toDomain(): UdhaarEntry {
    return UdhaarEntry(
        id = id,
        personId = personId,
        amount = amount,
        type = runCatching { UdhaarEntryType.valueOf(type) }.getOrDefault(UdhaarEntryType.GIVEN),
        note = note,
        date = date
    )
}

fun UdhaarEntry.toEntity(): UdhaarEntryEntity {
    return UdhaarEntryEntity(
        id = id,
        personId = personId,
        amount = amount,
        type = type.name,
        note = note,
        date = date
    )
}
