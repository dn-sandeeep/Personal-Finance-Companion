package com.sandeep.personalfinancecompanion.appfunctions

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.sandeep.personalfinancecompanion.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the discovery of the AI Agent capabilities by the Android system.
 * It registers dynamic shortcuts that Gemini/Assistant can use as "hints" 
 * for available App Functions.
 */
@Singleton
class AgentLifecycleManager @Inject constructor() {

    fun registerAgentCapabilities(context: Context) {
        // We register a dynamic shortcut that describes the core capability.
        // Even though App Functions are discovered automatically, having a shortcut
        // with semantic labels helps Gemini map natural language intents more reliably.
        
        val addExpenseShortcut = ShortcutInfoCompat.Builder(context, "add_expense_shortcut")
            .setShortLabel("Add Expense")
            .setLongLabel("Log an expense with voice")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(Intent(Intent.ACTION_VIEW).setPackage(context.packageName))
            .setCategories(setOf(
                "com.sandeep.personalfinancecompanion.category.FINANCE",
                "android.intent.category.APP_FINANCE"
            ))
            .build()

        ShortcutManagerCompat.addDynamicShortcuts(context, listOf(addExpenseShortcut))
    }
}
