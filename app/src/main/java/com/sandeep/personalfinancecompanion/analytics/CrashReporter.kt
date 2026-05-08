package com.sandeep.personalfinancecompanion.analytics

interface CrashReporter {
    fun setCollectionEnabled(enabled: Boolean)
    fun setUserProperty(name: String, value: String)
    fun recordNonFatal(throwable: Throwable)
}
