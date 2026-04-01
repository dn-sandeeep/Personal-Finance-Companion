package com.sandeep.personalfinancecompanion.domain.model

enum class Category(val displayName: String, val emoji: String) {
    FOOD("Food", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍️"),
    ENTERTAINMENT("Entertainment", "🎬"),
    BILLS("Bills", "📄"),
    HEALTH("Health", "💊"),
    EDUCATION("Education", "📚"),
    SALARY("Salary", "💰"),
    FREELANCE("Freelance", "💼"),
    INVESTMENT("Investment", "📈"),
    GIFT("Gift", "🎁"),
    OTHER("Other", "📌");

    companion object {
        fun expenseCategories(): List<Category> =
            listOf(FOOD, TRANSPORT, SHOPPING, ENTERTAINMENT, BILLS, HEALTH, EDUCATION, OTHER)

        fun incomeCategories(): List<Category> =
            listOf(SALARY, FREELANCE, INVESTMENT, GIFT, OTHER)
    }
}
