package com.sandeep.personalfinancecompanion.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["sourceFingerprint"])]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val notes: String,
    val peerName: String?,
    val isSettled: Boolean,
    val sourceType: String?,
    val sourceFingerprint: String?,
    val rawSourceText: String?
)
