package com.sandeep.personalfinancecompanion.analytics

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashReporter @Inject constructor(
    private val app: Application
) : CrashReporter {

    private val crashlytics = if (app.hasFirebaseConfig()) FirebaseCrashlytics.getInstance() else null

    init {
        crashlytics?.setCrashlyticsCollectionEnabled(false)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        crashlytics?.setCrashlyticsCollectionEnabled(enabled)
    }

    override fun setUserProperty(name: String, value: String) {
        crashlytics?.setCustomKey(name, value.take(MAX_VALUE_LENGTH))
    }

    override fun recordNonFatal(throwable: Throwable) {
        crashlytics?.recordException(throwable)
    }

    private companion object {
        const val MAX_VALUE_LENGTH = 100
    }
}

private fun Application.hasFirebaseConfig(): Boolean {
    return resources.getIdentifier("google_app_id", "string", packageName) != 0
}
