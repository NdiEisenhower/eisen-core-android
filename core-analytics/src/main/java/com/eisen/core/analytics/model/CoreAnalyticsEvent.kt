package com.eisen.core.analytics.model

object CoreAnalyticsEvent {

    // App Lifecycle
    const val APP_OPEN = "app_open"
    const val APP_UPDATE = "app_update"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val SHARE = "share"
    const val SEARCH = "search"

    // Game Events
    const val LEVEL_START = "level_start"
    const val LEVEL_END = "level_end"
    const val TUTORIAL_START = "tutorial_start"
    const val TUTORIAL_COMPLETE = "tutorial_complete"
    const val ACHIEVEMENT_UNLOCKED = "unlock_achievement"
    const val POST_SCORE = "post_score"

    // In-App Purchases & Monetization
    const val VIEW_ITEM = "view_item"
    const val BEGIN_CHECKOUT = "begin_checkout"
    const val PURCHASE = "purchase"
    const val REFUND = "refund"

    // Ads
    const val AD_IMPRESSION = "ad_impression"
    const val AD_CLICK = "ad_click"
    const val REWARD_EARNED = "reward_earned"

    // Common Parameter Keys
    object Param {
        const val LEVEL = "level"
        const val LEVEL_NAME = "level_name"
        const val SCORE = "score"
        const val SUCCESS = "success"
        const val ITEM_ID = "item_id"
        const val ITEM_NAME = "item_name"
        const val VALUE = "value"
        const val CURRENCY = "currency"
        const val METHOD = "method"
        const val AD_UNIT_ID = "ad_unit_id"
        const val AD_FORMAT = "ad_format"
        const val REASON = "reason"
        const val SCREEN_NAME = "screen_name"
    }
}

