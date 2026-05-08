package com.sandeep.personalfinancecompanion.analytics

interface AnalyticsTracker {
    fun setCollectionEnabled(enabled: Boolean)
    fun trackScreen(screenName: String, route: String)
    fun trackEvent(name: String, params: Map<String, Any?> = emptyMap())
}
