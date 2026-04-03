package com.sandeep.personalfinancecompanion.domain.model

enum class Currency(
    val code: String,
    val symbol: String,
    val label: String,
    val flag: String
) {
    INR("INR", "₹", "Indian Rupee", "🇮🇳"),
    USD("USD", "$", "US Dollar", "🇺🇸"),
    EUR("EUR", "€", "Euro", "🇪🇺"),
    GBP("GBP", "£", "British Pound", "🇬🇧"),
    JPY("JPY", "¥", "Japanese Yen", "🇯🇵"),
    AUD("AUD", "A$", "Australian Dollar", "🇦🇺"),
    CAD("CAD", "C$", "Canadian Dollar", "🇨🇦"),
    CHF("CHF", "Fr", "Swiss Franc", "🇨🇭"),
    CNY("CNY", "¥", "Chinese Yuan", "🇨🇳"),
    AED("AED", "د.إ", "UAE Dirham", "🇦🇪");

    companion object {
        fun fromCode(code: String?): Currency {
            return entries.find { it.code == code } ?: INR
        }
    }
}
