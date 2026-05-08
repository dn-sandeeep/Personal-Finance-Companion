package com.sandeep.personalfinancecompanion.analytics

object AnalyticsEvent {
    const val SCREEN_VIEW = "screen_view"
    const val NAV_ITEM_SELECTED = "nav_item_selected"
    const val PROFILE_OPENED = "profile_opened"
    const val ADD_TRANSACTION_CLICKED = "add_transaction_clicked"
    const val VOICE_AGENT_OPENED = "voice_agent_opened"
    const val TRANSACTION_FILTER_CHANGED = "transaction_filter_changed"
    const val TRANSACTION_ADD_STARTED = "transaction_add_started"
    const val TRANSACTION_ADD_SAVED = "transaction_add_saved"
    const val TRANSACTION_EDIT_OPENED = "transaction_edit_opened"
    const val TRANSACTION_EDIT_SAVED = "transaction_edit_saved"
    const val BUDGET_DIALOG_OPENED = "budget_dialog_opened"
    const val BUDGET_SAVED = "budget_saved"
    const val CATEGORY_SELECTED = "category_selected"
    const val DEBT_OPENED = "debt_opened"
    const val EXPORT_STARTED = "export_started"
    const val EXPORT_COMPLETED = "export_completed"
    const val EXPORT_FAILED = "export_failed"
    const val LANGUAGE_CHANGED = "language_changed"
    const val CURRENCY_CHANGED = "currency_changed"
    const val ANALYTICS_CONSENT_CHANGED = "analytics_consent_changed"
}

object AnalyticsParam {
    const val SCREEN_NAME = "screen_name"
    const val ROUTE = "route"
    const val SOURCE = "source"
    const val TYPE = "type"
    const val FILTER_TYPE = "filter_type"
    const val CATEGORY = "category"
    const val ENABLED = "enabled"
    const val LANGUAGE = "language"
    const val CURRENCY = "currency"
    const val RESULT = "result"
}
