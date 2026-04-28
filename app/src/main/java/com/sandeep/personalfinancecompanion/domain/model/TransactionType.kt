package com.sandeep.personalfinancecompanion.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    BORROWED, // Udhar Liya
    LENT,      // Udhar Diya
    BORROWED_REPAYMENT, // Karz Wapas Kiya (Money OUT)
    LENT_REPAYMENT      // Udhar Wapas Mila (Money IN)
}
