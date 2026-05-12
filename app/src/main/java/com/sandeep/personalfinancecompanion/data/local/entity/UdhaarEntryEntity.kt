package com.sandeep.personalfinancecompanion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "udhaar_entries",
    foreignKeys = [
        ForeignKey(
            entity = UdhaarPersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personId"])]
)
data class UdhaarEntryEntity(
    @PrimaryKey
    val id: String,
    val personId: String,
    val amount: Double,
    val type: String,
    val note: String,
    val date: Long
)
