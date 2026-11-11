package com.eisen.sample

import com.eisen.core.turnkey.TurnkeyAdConfig
import com.eisen.core.turnkey.TurnkeyBillingConfig
import com.eisen.core.turnkey.TurnkeyConfig
import com.eisen.core.turnkey.TurnkeyFeature
import com.eisen.core.turnkey.TurnkeyGamesConfig
import com.eisen.core.turnkey.TurnkeyNotificationConfig
import com.eisen.core.turnkey.TurnkeyWebViewActivity
import com.eisen.core.webview.WebViewConfig

/**
 * Example Turnkey Game/App Activity.
 *
 * This single class represents a 100% complete, production-ready Android app
 * with Google & Huawei dual-store support, Ads, In-App Purchases, Game Services,
 * Push Notifications, Screen Sharing, Biometrics, and In-App Reviews.
 */
class SampleTurnkeyActivity : TurnkeyWebViewActivity() {

    override fun getTurnkeyConfig(): TurnkeyConfig {
        return TurnkeyConfig(
            url = "https://play.eisen.com/demo",
            features = setOf(
                TurnkeyFeature.BILLING,
                TurnkeyFeature.ADS,
                TurnkeyFeature.GAMES,
                TurnkeyFeature.SHARE,
                TurnkeyFeature.NOTIFICATIONS,
                TurnkeyFeature.ANALYTICS,
                TurnkeyFeature.REVIEWS,
                TurnkeyFeature.BIOMETRICS
            ),
            webViewConfig = WebViewConfig(
                url = "https://play.eisen.com/demo",
                fullScreen = true,
                blockWebAds = true,
                hideWebTopBar = true
            ),
            ads = TurnkeyAdConfig(
                adMobBannerId = "ca-app-pub-3940256099942544/6300978111",
                adMobInterstitialId = "ca-app-pub-3940256099942544/1033173712",
                adMobRewardedId = "ca-app-pub-3940256099942544/5224354917",
                enableUmpConsent = true,
                autoPreload = true
            ),
            billing = TurnkeyBillingConfig(
                productIds = listOf("coins_100", "remove_ads", "starter_pack"),
                subscriptionIds = listOf("vip_monthly")
            ),
            games = TurnkeyGamesConfig(
                autoSignIn = true
            ),
            notifications = TurnkeyNotificationConfig(
                defaultChannelId = "game_alerts",
                defaultChannelName = "Game Alerts & Live Events",
                autoRequestPermission = true
            )
        )
    }
}
