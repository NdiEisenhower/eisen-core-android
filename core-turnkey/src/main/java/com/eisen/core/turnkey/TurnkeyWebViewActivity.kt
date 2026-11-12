package com.eisen.core.turnkey

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.fragment.app.FragmentActivity
import com.eisen.core.ads.AdBridge
import com.eisen.core.ads.AdFactory
import com.eisen.core.ads.IAdManager
import com.eisen.core.ads.UmpConsentManager
import com.eisen.core.analytics.AnalyticsFactory
import com.eisen.core.analytics.IAnalyticsManager
import com.eisen.core.analytics.bridge.AnalyticsJsBridge
import com.eisen.core.analytics.model.CoreAnalyticsEvent
import com.eisen.core.auth.AuthBridge
import com.eisen.core.auth.AuthFactory
import com.eisen.core.auth.IAuthManager
import com.eisen.core.billing.BillingBridge
import com.eisen.core.billing.BillingFactory
import com.eisen.core.billing.IBillingManager
import com.eisen.core.common.bridge.JsBridgeRegistry
import com.eisen.core.games.GameFactory
import com.eisen.core.games.IGameManager
import com.eisen.core.games.bridge.GameJsBridge
import com.eisen.core.notifications.IPushManager
import com.eisen.core.notifications.PushFactory
import com.eisen.core.notifications.bridge.PushJsBridge
import com.eisen.core.notifications.channel.NotificationChannelHelper
import com.eisen.core.notifications.model.NotificationChannelConfig
import com.eisen.core.permissions.PermissionHelper
import com.eisen.core.review.IReviewManager
import com.eisen.core.review.ReviewBridge
import com.eisen.core.review.ReviewManagerFactory
import com.eisen.core.security.bridge.BiometricJsBridge
import com.eisen.core.share.bridge.ShareJsBridge
import com.eisen.core.updater.AppDistributionManager
import com.eisen.core.updater.PlayUpdateManager
import com.eisen.core.updater.UpdateBridge
import com.eisen.core.webview.AudioFocusHelper
import com.eisen.core.webview.CrashJsBridge
import com.eisen.core.webview.EngineJsBridge
import com.eisen.core.webview.ui.CoreWebView

/**
 * Pure Jetpack Compose 1-Class Turnkey Activity.
 *
 * Provides a production-ready, zero-boilerplate Android App / Game wrapper.
 * Developers simply override [getTurnkeyConfig] declaring the URL and the desired
 * features (Billing, Ads, Games, Push, Analytics, Biometrics, Share, Reviews).
 *
 * Includes:
 * - Android 15/16 Edge-to-Edge compliance.
 * - Google UMP GDPR Consent helper.
 * - Tactile game haptics (click, heavy, tick, double).
 * - Automatic dual-store support (Google Play & Huawei AppGallery).
 * - Runtime Notification permission handling for Android 13+.
 * - Deep linking auto-routing into WebView or JS `window.onNotificationDeepLink(url)`.
 * - Audio focus management (auto-pauses on phone call or backgrounding).
 */
abstract class TurnkeyWebViewActivity : FragmentActivity() {

    protected val bridgeRegistry = JsBridgeRegistry()
    protected var webView: WebView? = null
    protected val audioFocusHelper by lazy { AudioFocusHelper(this) { webView } }

    var adManager: IAdManager? = null
        private set
    var billingManager: IBillingManager? = null
        private set
    var authManager: IAuthManager? = null
        private set
    var pushManager: IPushManager? = null
        private set
    var analyticsManager: IAnalyticsManager? = null
        private set
    var gameManager: IGameManager? = null
        private set
    var reviewManager: IReviewManager? = null
        private set
    var playUpdateManager: PlayUpdateManager? = null
        private set

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pushManager?.getPushToken { result ->
                if (result.isSuccess && result.token != null) {
                    analyticsManager?.setUserProperty("push_token", result.token!!)
                }
            }
        }
    }

    /**
     * Override this method to provide the Turnkey configuration.
     */
    abstract fun getTurnkeyConfig(): TurnkeyConfig

    /**
     * Optional hook to register custom JS bridges or native APIs.
     */
    open fun onCreateCustomBridges(registry: JsBridgeRegistry) {}

    /**
     * Optional hook called after all turnkey features and bridges are initialized.
     */
    open fun onTurnkeyReady(bridges: JsBridgeRegistry) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val config = getTurnkeyConfig()

        // 1. Core Bridges
        bridgeRegistry.register(EngineJsBridge(this))
        bridgeRegistry.register(CrashJsBridge())

        // 2. Initialize Analytics
        if (config.features.contains(TurnkeyFeature.ANALYTICS)) {
            val analytics = AnalyticsFactory.create(this, config.analytics.store)
            analyticsManager = analytics
            bridgeRegistry.register(AnalyticsJsBridge(analytics))
            if (config.analytics.logAppOpen) {
                analytics.logEvent(CoreAnalyticsEvent.APP_OPEN)
            }
        }

        // 3. Initialize Push Notifications & Channels
        if (config.features.contains(TurnkeyFeature.NOTIFICATIONS)) {
            NotificationChannelHelper.createDefaultChannels(this)
            NotificationChannelHelper.createChannel(
                this,
                NotificationChannelConfig(
                    id = config.notifications.defaultChannelId,
                    name = config.notifications.defaultChannelName,
                    description = config.notifications.defaultChannelDescription
                )
            )

            val push = PushFactory.create(this, config.notifications.store, config.notifications.huaweiAppId)
            pushManager = push
            bridgeRegistry.register(PushJsBridge(this, push, webView))

            if (config.notifications.autoRequestPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!PermissionHelper.hasNotificationPermission(this)) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        // 4. Initialize In-App Billing
        if (config.features.contains(TurnkeyFeature.BILLING)) {
            val billing = BillingFactory.create(this, config.billing.store)
            billingManager = billing
            billing.initialize {
                if (config.billing.productIds.isNotEmpty()) {
                    billing.queryProducts(config.billing.productIds)
                }
                if (config.billing.subscriptionIds.isNotEmpty()) {
                    billing.queryProducts(config.billing.subscriptionIds, isSubscription = true)
                }
            }
            bridgeRegistry.register(BillingBridge(this, billing))
        }

        // 5. Initialize Ads & Google UMP GDPR Consent
        if (config.features.contains(TurnkeyFeature.ADS)) {
            val ads = AdFactory.create(this, config.ads.store)
            adManager = ads

            if (config.ads.enableUmpConsent) {
                UmpConsentManager.requestConsent(this) { canRequestAds ->
                    if (canRequestAds) {
                        initAndPreloadAds(ads, config.ads)
                    }
                }
            } else {
                initAndPreloadAds(ads, config.ads)
            }

            bridgeRegistry.register(AdBridge(this, ads))
        }

        // 6. Initialize Game Services (Leaderboards, Achievements, Cloud Save)
        if (config.features.contains(TurnkeyFeature.GAMES)) {
            val games = GameFactory.create(this, config.games.store)
            gameManager = games
            if (config.games.autoSignIn) {
                games.signIn(this) { success, _, _ ->
                    if (success) {
                        analyticsManager?.logEvent("games_sign_in_success")
                    }
                }
            }
            bridgeRegistry.register(GameJsBridge(this, games, webView))
        }

        // 7. Initialize Auth
        if (config.features.contains(TurnkeyFeature.AUTH)) {
            val auth = AuthFactory.create()
            authManager = auth
            bridgeRegistry.register(AuthBridge(this, auth))
        }

        // 8. Initialize In-App Review
        if (config.features.contains(TurnkeyFeature.REVIEWS)) {
            val review = ReviewManagerFactory.create(this, config.reviews.store)
            reviewManager = review
            bridgeRegistry.register(ReviewBridge(this, review))
        }

        // 9. Initialize Share
        if (config.features.contains(TurnkeyFeature.SHARE)) {
            bridgeRegistry.register(ShareJsBridge(this, webView))
        }

        // 10. Initialize Biometrics & Secure Storage
        if (config.features.contains(TurnkeyFeature.BIOMETRICS)) {
            bridgeRegistry.register(BiometricJsBridge(this, webView))
        }

        // 11. Initialize Google Play In-App Updates & Firebase App Distribution
        if (config.features.contains(TurnkeyFeature.UPDATES) || config.features.contains(TurnkeyFeature.DISTRIBUTION)) {
            val updateMgr = PlayUpdateManager(this)
            playUpdateManager = updateMgr
            bridgeRegistry.register(UpdateBridge(this, updateMgr))

            if (config.features.contains(TurnkeyFeature.UPDATES) && config.updates.autoCheckPlayUpdates) {
                updateMgr.checkForUpdate()
            }

            if (config.features.contains(TurnkeyFeature.DISTRIBUTION)) {
                if (config.updates.promptTesterSignIn && !AppDistributionManager.isTesterSignedIn()) {
                    AppDistributionManager.signInTester()
                }
                if (config.updates.autoCheckDistribution) {
                    AppDistributionManager.updateIfNewReleaseAvailable()
                }
            }
        }

        // 12. Allow developer custom bridges
        onCreateCustomBridges(bridgeRegistry)

        // 13. Render Pure Jetpack Compose UI
        setContent {
            MaterialTheme {
                CoreWebView(
                    config = config.webViewConfig,
                    bridgeRegistry = bridgeRegistry,
                    onWebViewCreated = { wv ->
                        webView = wv
                        audioFocusHelper.requestAudioFocus()
                        handleDeepLinkIntent(intent)
                        onTurnkeyReady(bridgeRegistry)
                    }
                )
            }
        }
    }

    private fun initAndPreloadAds(ads: IAdManager, adConfig: TurnkeyAdConfig) {
        ads.initialize(this) {
            if (adConfig.autoPreload) {
                val interstitialId = adConfig.adMobInterstitialId ?: adConfig.huaweiInterstitialId
                interstitialId?.let { ads.loadInterstitial(this, it) }

                val rewardedId = adConfig.adMobRewardedId ?: adConfig.huaweiRewardedId
                rewardedId?.let { ads.loadRewarded(this, it) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        webView?.resumeTimers()
        audioFocusHelper.requestAudioFocus()
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
        webView?.pauseTimers()
        audioFocusHelper.abandonAudioFocus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    /**
     * Inspects incoming intent for deep link extras from push notifications or external URIs.
     */
    protected open fun handleDeepLinkIntent(intent: Intent?) {
        val deepLink = intent?.getStringExtra("eisen_extra_deep_link")
            ?: intent?.dataString
            ?: intent?.getStringExtra("deep_link")
            ?: intent?.getStringExtra("path")
            ?: intent?.getStringExtra("url")

        if (!deepLink.isNullOrBlank()) {
            onDeepLinkReceived(deepLink)
        }
    }

    /**
     * Default deep link routing to WebView or frontend JavaScript event.
     */
    protected open fun onDeepLinkReceived(deepLink: String) {
        val wv = webView ?: return
        when {
            deepLink.startsWith("http://") || deepLink.startsWith("https://") -> {
                wv.loadUrl(deepLink)
            }
            deepLink.startsWith("/") -> {
                val base = getTurnkeyConfig().url.trimEnd('/')
                wv.loadUrl("$base$deepLink")
            }
            else -> {
                val safeLink = deepLink.replace("'", "\\'")
                wv.evaluateJavascript(
                    """
                    (function() {
                        if (typeof window.onNotificationDeepLink === 'function') {
                            window.onNotificationDeepLink('$safeLink');
                        } else if (typeof window.onNotificationClicked === 'function') {
                            window.onNotificationClicked('$safeLink', {});
                        } else {
                            window.dispatchEvent(new CustomEvent('eisen:deeplink', { detail: { path: '$safeLink' } }));
                        }
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }
    }

    override fun onDestroy() {
        audioFocusHelper.abandonAudioFocus()
        bridgeRegistry.detachAll()
        billingManager?.destroy()
        playUpdateManager?.destroy()
        webView?.destroy()
        super.onDestroy()
    }
}
