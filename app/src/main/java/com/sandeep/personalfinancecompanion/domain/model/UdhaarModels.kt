package com.sandeep.personalfinancecompanion.domain.model

enum class UdhaarEntryType {
    GIVEN,
    TAKEN,
    RECEIVED_BACK,
    PAID_BACK
}

data class UdhaarPerson(
    val id: String,
    val name: String,
    val phoneNumber: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

data class UdhaarEntry(
    val id: String,
    val personId: String,
    val amount: Double,
    val type: UdhaarEntryType,
    val note: String = "",
    val date: Long
)

data class UdhaarPersonSummary(
    val person: UdhaarPerson,
    val netAmount: Double,
    val isOwedToYou: Boolean,
    val history: List<UdhaarEntry>
)

data class UdhaarOverview(
    val totalReceivable: Double = 0.0,
    val totalPayable: Double = 0.0,
    val activePeople: Int = 0
)
