package com.eisen.core.turnkey

import com.eisen.core.ads.AdStore
import com.eisen.core.analytics.AnalyticsStore
import com.eisen.core.billing.BillingStore
import com.eisen.core.games.GameStore
import com.eisen.core.notifications.PushStore
import com.eisen.core.review.ReviewStore
import com.eisen.core.webview.WebViewConfig

/**
 * Modular turnkey features that can be enabled with one click/declaration.
 */
enum class TurnkeyFeature {
    /** Google Play Billing 8.3 & Huawei HMS IAP 6.13 */
    BILLING,
    /** Firebase Auth & Google Sign-In */
    AUTH,
    /** Google AdMob 25.5, Huawei Ads 13.4 & UMP GDPR Consent */
    ADS,
    /** Google Play Games v2 & Huawei GameCenter */
    GAMES,
    /** Android Sharesheet, Viral Score & Screenshot Capture */
    SHARE,
    /** Firebase Cloud Messaging & Huawei Push Kit 6.13 */
    NOTIFICATIONS,
    /** Google Firebase Analytics & Huawei HiAnalytics 6.12 */
    ANALYTICS,
    /** In-App Review Flow (Play Review & Huawei Market) */
    REVIEWS,
    /** Biometric Prompt (Fingerprint/Face) & Encrypted AES-256 Storage */
    BIOMETRICS
}

/**
 * Ad configuration for TurnkeyWebViewActivity.
 */
data class TurnkeyAdConfig(
    val adMobBannerId: String? = null,
    val adMobInterstitialId: String? = null,
    val adMobRewardedId: String? = null,
    val huaweiBannerId: String? = null,
    val huaweiInterstitialId: String? = null,
    val huaweiRewardedId: String? = null,
    val enableUmpConsent: Boolean = true,
    val autoPreload: Boolean = true,
    val store: AdStore = AdStore.AUTO
)

/**
 * In-app billing configuration for TurnkeyWebViewActivity.
 */
data class TurnkeyBillingConfig(
    val productIds: List<String> = emptyList(),
    val subscriptionIds: List<String> = emptyList(),
    val store: BillingStore = BillingStore.AUTO
)

/**
 * Push notifications configuration for TurnkeyWebViewActivity.
 */
data class TurnkeyNotificationConfig(
    val defaultChannelId: String = "general_notifications",
    val defaultChannelName: String = "Notifications",
    val defaultChannelDescription: String = "General notifications and updates",
    val huaweiAppId: String? = null,
    val autoRequestPermission: Boolean = true,
    val store: PushStore = PushStore.AUTO
)

/**
 * Analytics configuration for TurnkeyWebViewActivity.
 */
data class TurnkeyAnalyticsConfig(
    val store: AnalyticsStore = AnalyticsStore.AUTO,
    val logAppOpen: Boolean = true
)

/**
 * Games services configuration for TurnkeyWebViewActivity.
 */
data class TurnkeyGamesConfig(
    val autoSignIn: Boolean = true,
    val store: GameStore = GameStore.AUTO
)

/**
 * In-App Review configuration for TurnkeyWebViewActivity.
 */
data class TurnkeyReviewConfig(
    val store: ReviewStore = ReviewStore.AUTO
)

/**
 * Master Turnkey configuration.
 *
 * Simply provide the URL to load and specify which features your app needs.
 * All bridges, listeners, lifecycle handlers, and store clients are automatically wired up.
 */
data class TurnkeyConfig(
    val url: String,
    val features: Set<TurnkeyFeature> = setOf(
        TurnkeyFeature.SHARE,
        TurnkeyFeature.REVIEWS,
        TurnkeyFeature.NOTIFICATIONS,
        TurnkeyFeature.ANALYTICS
    ),
    val webViewConfig: WebViewConfig = WebViewConfig(
        url = url,
        fullScreen = false,
        blockWebAds = true,
        hideWebTopBar = false
    ),
    val ads: TurnkeyAdConfig = TurnkeyAdConfig(),
    val billing: TurnkeyBillingConfig = TurnkeyBillingConfig(),
    val notifications: TurnkeyNotificationConfig = TurnkeyNotificationConfig(),
    val analytics: TurnkeyAnalyticsConfig = TurnkeyAnalyticsConfig(),
    val games: TurnkeyGamesConfig = TurnkeyGamesConfig(),
    val reviews: TurnkeyReviewConfig = TurnkeyReviewConfig()
)
