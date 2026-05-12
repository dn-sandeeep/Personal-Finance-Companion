package com.sandeep.personalfinancecompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "udhaar_people")
data class UdhaarPersonEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val createdAt: Long,
    val updatedAt: Long
)
