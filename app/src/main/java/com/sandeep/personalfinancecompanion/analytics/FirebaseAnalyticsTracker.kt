package com.sandeep.personalfinancecompanion.analytics

import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    app: Application
) : AnalyticsTracker {

    private val analytics = if (app.hasFirebaseConfig()) FirebaseAnalytics.getInstance(app) else null
    @Volatile private var collectionEnabled = true

    init {
        analytics?.setAnalyticsCollectionEnabled(true)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        collectionEnabled = enabled
        analytics?.setAnalyticsCollectionEnabled(enabled)
    }

    override fun trackScreen(screenName: String, route: String) {
        trackEvent(
            AnalyticsEvent.SCREEN_VIEW,
            mapOf(
                AnalyticsParam.SCREEN_NAME to screenName,
                AnalyticsParam.ROUTE to route
            )
        )
    }

    override fun trackEvent(name: String, params: Map<String, Any?>) {
        if (!collectionEnabled) return

        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value.take(MAX_PARAM_LENGTH))
                is Int -> bundle.putLong(key, value.toLong())
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putDouble(key, value.toDouble())
                is Boolean -> bundle.putString(key, if (value) "true" else "false")
                null -> Unit
                else -> bundle.putString(key, value.toString().take(MAX_PARAM_LENGTH))
            }
        }
        analytics?.logEvent(name, bundle)
    }

    private companion object {
        const val MAX_PARAM_LENGTH = 100
    }
}

private fun Application.hasFirebaseConfig(): Boolean {
    return resources.getIdentifier("google_app_id", "string", packageName) != 0
}
