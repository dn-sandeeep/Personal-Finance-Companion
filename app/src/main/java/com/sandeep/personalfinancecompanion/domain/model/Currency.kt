package com.sandeep.personalfinancecompanion.domain.model

enum class Currency(
    val code: String,
    val symbol: String,
    val label: String,
    val flag: String,
    val rateInINR: Double 
) {
    INR("INR", "₹", "Indian Rupee", "🇮🇳", 1.0),
    USD("USD", "$", "US Dollar", "🇺🇸", 83.0),
    EUR("EUR", "€", "Euro", "🇪🇺", 90.0),
    GBP("GBP", "£", "British Pound", "🇬🇧", 105.0),
    JPY("JPY", "¥", "Japanese Yen", "🇯🇵", 0.55),
    AUD("AUD", "A$", "Australian Dollar", "🇦🇺", 54.0),
    CAD("CAD", "C$", "Canadian Dollar", "🇨🇦", 61.0),
    CHF("CHF", "Fr", "Swiss Franc", "🇨🇭", 92.0),
    CNY("CNY", "¥", "Chinese Yuan", "🇨🇳", 11.5),
    AED("AED", "د.إ", "UAE Dirham", "🇦🇪", 22.6);

    companion object {
        fun fromCode(code: String?): Currency {
            return entries.find { it.code == code } ?: INR
        }

        fun convert(amount: Double, from: Currency, to: Currency): Double {
            if (from == to) return amount
            val amountInINR = amount * from.rateInINR
            return amountInINR / to.rateInINR
        }
    }
}
