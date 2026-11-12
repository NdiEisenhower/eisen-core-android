# Eisen Core Android

Modular, 100% Jetpack Compose Android SDK ecosystem designed for rapid app and HTML5 game development. No legacy XML views. Supports both **Google Play** and **Huawei AppGallery** out of the box with the latest 2026 APIs.

---

## Modules

| Module | Artifact | Description |
| :--- | :--- | :--- |
| **`core-turnkey`** | `:core-turnkey` | Master 1-class boilerplate-free turnkey activity (`TurnkeyWebViewActivity`). Enables Billing, Ads, Games, Share, Push, Analytics, Biometrics, and Reviews with a single declarative configuration class. |
| **`core-webview`** | `:core-webview` | 100% Jetpack Compose turnkey WebView (`@Composable CoreWebView` + `QuickWebViewActivity`). File uploads, full-screen video, D-Pad/TV controls, Compose Material3 progress and error state, dynamic Bottom Navigation on the fly, game haptics, and JS bridge registry. |
| **`core-billing`** | `:core-billing` | Unified In-App Purchases (**Google Play Billing 8.3** & **Huawei HMS IAP 6.13**) + WebView `window.AndroidBilling` bridge. |
| **`core-auth`** | `:core-auth` | Authentication (Google Sign-In, Firebase Auth, Guest) + WebView `window.AndroidAuth` bridge. |
| **`core-ads`** | `:core-ads` | Monetization (**Google AdMob 25.5** & **Huawei HMS Ads 13.4**), **Google UMP GDPR Consent Helper**, and WebView `window.AndroidAds` bridge. |
| **`core-review`** | `:core-review` | In-App Review API (**Google Play In-App Review 2.0.2** & **Huawei AppGallery In-App Comments**) + `window.AndroidReview` bridge. |
| **`core-notifications`** | `:core-notifications` | Push Notifications (**Firebase Cloud Messaging** & **Huawei HMS Push Kit 6.13**), Android 8+ Notification Channel creation, Deep Linking routing into WebView, and `window.AndroidPush` bridge. |
| **`core-analytics`** | `:core-analytics` | Unified Analytics (**Google Firebase Analytics** & **Huawei HiAnalytics 6.12**), standard event models, and `window.AndroidAnalytics` bridge. |
| **`core-games`** | `:core-games` | Unified Game Services (**Google Play Games v2** & **Huawei GameCenter**), auto sign-in, global leaderboards, achievements, and `window.AndroidGames` bridge. |
| **`core-share`** | `:core-share` | Viral score sharing, system share sheet, and one-tap Compose/WebView screenshot capture via `window.AndroidShare`. |
| **`core-security`** | `:core-security` | Biometric authentication (Fingerprint / Face Unlock via `BiometricPrompt`), Encrypted SharedPreferences keystore, and `window.AndroidBiometrics` bridge. |
| **`core-permissions`** | `:core-permissions` | Declarative Jetpack Compose permission dialogs and Android 13+ Notification permission requester. |
| **`core-updater`** | `:core-updater` | Google Play In-App Updates (immediate & flexible), Google Firebase App Distribution (in-app tester alerts & feedback), Jetpack Compose `ForceUpdateScreen` dialog, and `window.AndroidUpdates` bridge. |
| **`core-common`** | `:core-common` | Base `JsBridge`, `JsBridgeRegistry`, `CoreResult`, and Network monitoring. |

---

## 1-Class Turnkey Setup (Recommended)

Build an entire production-grade Google Play & Huawei AppGallery app or game in **one single class**:

### 1. Add Single Dependency
```kotlin
dependencies {
    // Bundles all 13 modules into one turnkey package:
    implementation("com.github.NdiEisenhower.eisen-core-android:core-turnkey:v1.0.0")
}
```

### 2. Declare Your Activity
```kotlin
class MainActivity : TurnkeyWebViewActivity() {

    override fun getTurnkeyConfig(): TurnkeyConfig {
        return TurnkeyConfig(
            url = "https://mycoolgame.com",
            features = setOf(
                TurnkeyFeature.BILLING,
                TurnkeyFeature.ADS,
                TurnkeyFeature.GAMES,
                TurnkeyFeature.SHARE,
                TurnkeyFeature.NOTIFICATIONS,
                TurnkeyFeature.ANALYTICS,
                TurnkeyFeature.REVIEWS,
                TurnkeyFeature.BIOMETRICS,
                TurnkeyFeature.UPDATES,
                TurnkeyFeature.DISTRIBUTION
            ),
            webViewConfig = WebViewConfig(
                url = "https://mycoolgame.com",
                fullScreen = true,      // Android 15/16 edge-to-edge immersive full-screen
                blockWebAds = true,     // Drops AdSense/DoubleClick web scripts automatically
                hideWebTopBar = true    // Injects CSS to hide site headers/menus
            ),
            ads = TurnkeyAdConfig(
                adMobBannerId = "ca-app-pub-3940256099942544/6300978111",
                adMobInterstitialId = "ca-app-pub-3940256099942544/1033173712",
                adMobRewardedId = "ca-app-pub-3940256099942544/5224354917",
                enableUmpConsent = true, // Collects EU GDPR consent before requesting ads
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
                autoRequestPermission = true // Automatically prompts POST_NOTIFICATIONS on Android 13+
            ),
            updates = TurnkeyUpdateConfig(
                enablePlayUpdates = true,
                enableDistributionAlerts = true
            )
        )
    }
}
```

This single class automatically wires up:
- Android 15/16 Edge-to-Edge system layout.
- Google UMP GDPR Consent dialogs before showing AdMob ads.
- Tactile game haptics via `window.AndroidEngine.hapticClick()`, `hapticHeavy()`, `hapticTick()`, `hapticDouble()`.
- Dual-store billing (Google Play 8.3 & Huawei IAP 6.13).
- Dual-store Game Services (Play Games v2 & Huawei GameCenter auto sign-in).
- Push notification channel registration and deep linking auto-routing.
- Audio focus management (auto-pauses HTML5 audio on phone call or backgrounding).
- Google Play In-App Updates and Firebase App Distribution tester alerts.
- Automatic page status telemetry injection (`window.__EISEN_APP_STATUS__` and `eisen:appstatus` event).
- All JavaScript bridges (`AndroidEngine`, `AndroidBilling`, `AndroidAds`, `AndroidGames`, `AndroidPush`, `AndroidAnalytics`, `AndroidReview`, `AndroidShare`, `AndroidBiometrics`, `AndroidUpdates`, `AndroidCrash`).

---

## Google Play & Huawei Dual-Flavor Architecture

Whether you build single-store apps or dual-store flavors (`google` and `huawei`), the SDK automatically detects the environment or lets you configure it explicitly via factories:

```kotlin
// Billing: Works on Google Play & Huawei
val billing = BillingFactory.create(context, store = BillingStore.AUTO) // or BillingStore.GOOGLE / BillingStore.HUAWEI
billing.initialize()

// Ads: Works on AdMob & Huawei Ads
val ads = AdFactory.create(context, store = AdStore.AUTO)
ads.initialize(activity)

// In-App Review: Works on Google Play Review API & Huawei AppGallery Comments
val review = ReviewManagerFactory.create(context, flavor = StoreFlavor.AUTO, huaweiAppId = "123456789")
review.launchReviewFlow(activity)

// Push Notifications: Works on FCM & Huawei Push Kit
val push = PushFactory.create(context, store = PushStore.AUTO)
push.initialize(activity)
push.getPushToken { result -> println("Push token: ${result.token}") }

// Analytics: Works on Firebase & Huawei HiAnalytics
val analytics = AnalyticsFactory.create(context, store = AnalyticsStore.AUTO)
analytics.logEvent(CoreAnalyticsEvent.LEVEL_START, mapOf(CoreAnalyticsEvent.Param.LEVEL to 1))

// Game Services: Play Games v2 & Huawei GameCenter
val games = GameFactory.create(context, store = GameStore.AUTO)
games.initialize(activity)
games.signIn(activity) { success, player, error -> println("Signed in: ${player?.displayName}") }

// Biometrics: Fingerprint & Face Unlock
BiometricHelper.authenticate(activity, title = "Unlock Wallet", onSuccess = { println("Unlocked") }, onError = { code, err -> })

// Share: Score & Screen Capture
ShareHelper.shareScore(context, gameName = "Block Fusion", score = 4500)
```

---

## Quick Start: Individual Module Integration

If you only need specific SDK modules instead of `:core-turnkey`:

### 1. Add Repositories
In your new app's `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://developer.huawei.com/repo/") // for Huawei HMS
        maven(url = "https://jitpack.io")
    }
}
```

### 2. Add Desired Dependencies
In your new app's `app/build.gradle.kts`:
```kotlin
dependencies {
    // 100% Compose WebView
    implementation("com.github.NdiEisenhower.eisen-core-android:core-webview:v1.0.0")

    // Optional: In-App Purchases (Google + Huawei)
    implementation("com.github.NdiEisenhower.eisen-core-android:core-billing:v1.0.0")

    // Optional: Login / Auth
    implementation("com.github.NdiEisenhower.eisen-core-android:core-auth:v1.0.0")

    // Optional: Ads (AdMob + Huawei Ads + Google UMP Consent)
    implementation("com.github.NdiEisenhower.eisen-core-android:core-ads:v1.0.0")

    // Optional: In-App Review API (Google Play + Huawei)
    implementation("com.github.NdiEisenhower.eisen-core-android:core-review:v1.0.0")

    // Optional: Push Notifications & Deep Linking (Firebase + Huawei Push)
    implementation("com.github.NdiEisenhower.eisen-core-android:core-notifications:v1.0.0")

    // Optional: Analytics (Firebase + Huawei HiAnalytics)
    implementation("com.github.NdiEisenhower.eisen-core-android:core-analytics:v1.0.0")

    // Optional: Game Services (Play Games v2 + Huawei Game)
    implementation("com.github.NdiEisenhower.eisen-core-android:core-games:v1.0.0")

    // Optional: Viral Sharing & Screenshot Capture
    implementation("com.github.NdiEisenhower.eisen-core-android:core-share:v1.0.0")

    // Optional: Biometrics & Secure Storage
    implementation("com.github.NdiEisenhower.eisen-core-android:core-security:v1.0.0")

    // Optional: Compose Permission Requester
    implementation("com.github.NdiEisenhower.eisen-core-android:core-permissions:v1.0.0")

    // Optional: Remote Force Update
    implementation("com.github.NdiEisenhower.eisen-core-android:core-updater:v1.0.0")
}
```

### 3. Option A: Pure Jetpack Compose Screen
Drop `@Composable CoreWebView` directly into any Compose layout or Scaffold. You can also define **Bottom Navigation on the fly**:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val config = WebViewConfig(
                    url = "https://mycoolgame.com",
                    enableDpadFocus = true,
                    showProgressBar = true,
                    bottomNavItems = listOf(
                        BottomNavItem(id = "home", label = "Home", iconName = "home", targetUrl = "https://mycoolgame.com"),
                        BottomNavItem(id = "store", label = "Store", iconName = "store", targetUrl = "https://mycoolgame.com/store", badge = "SALE"),
                        BottomNavItem(id = "profile", label = "Profile", iconName = "profile", targetUrl = "https://mycoolgame.com/profile")
                    )
                )

                val state = rememberCoreWebViewState(config)
                CoreWebView(config = config, state = state)
            }
        }
    }
}
```

### 3. Option B: 1-Line Drop-in Activity
```kotlin
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QuickWebViewActivity.launch(this, "https://mycoolgame.com")
        finish()
    }
}
```

### 3. Option C: Offline Local HTML5 Bundle (with WebViewAssetLoader)
Avoid `file:///` restrictions and maintain full `localStorage`/IndexedDB support:
```kotlin
QuickWebViewActivity.launch(this, WebViewConfig.fromAsset("index.html"))
```

---

## AdSense Removal & App Store Compliance

Google Play and Huawei AppGallery prohibit web AdSense ads inside apps. The SDK provides two ways to handle this:

### When You DO NOT Control the Website (Native Blocking)
Set `blockWebAds = true`. The SDK intercepts and drops all AdSense/DoubleClick network requests and automatically collapses ad spaces using CSS injection:

```kotlin
val config = WebViewConfig(
    url = "https://externalgame.com",
    blockWebAds = true // Automatically blocks AdSense & hides ad containers
)
// Or via QuickWebViewActivity:
QuickWebViewActivity.launch(context, "https://externalgame.com", blockWebAds = true)
```

### When You DO Control the Website (Custom User-Agent Detection)
The SDK automatically appends `EisenApp/1.0` to the WebView's `User-Agent`. In your website's JavaScript, check for this token to bypass AdSense and trigger native ads:

```javascript
const isApp = navigator.userAgent.includes("EisenApp");

if (isApp) {
    // Inside the app: Call native AdMob or Huawei Ads via the bridge
    if (window.AndroidAds) {
        window.AndroidAds.showBanner("BOTTOM");
    }
} else {
    // Normal browser: Load regular web AdSense
    loadWebAdSense();
}
```

---

## Google UMP GDPR Consent Management

Serving AdMob ads in the European Economic Area (EEA) and the UK requires user consent under GDPR. The `:core-ads` module integrates Google User Messaging Platform (UMP) 3.1:

```kotlin
// In Native Kotlin:
UmpConsentManager.requestConsent(activity) { canRequestAds ->
    if (canRequestAds) {
        // Safe to initialize AdMob and load ads
        adManager.initialize(activity)
    }
}

// Show Privacy Options later from in-game settings:
if (UmpConsentManager.isPrivacyOptionsRequired(context)) {
    UmpConsentManager.showPrivacyOptionsForm(activity) {
        // Privacy form closed
    }
}
```

And directly accessible via JavaScript:
```javascript
// Check if user is eligible to modify privacy settings (e.g. GDPR region):
if (window.AndroidAds.isPrivacyOptionsRequired()) {
    showPrivacySettingsButton();
}

// Open the Google Privacy / Consent form:
window.AndroidAds.showPrivacyOptions();
window.onPrivacyOptionsClosed = () => {
    console.log("Consent options updated");
};
```

---

## Tactile Game Haptics

Give your HTML5 games physical tactile feedback using Android 10+ predefined vibration effects (with seamless fallback on older devices):

```javascript
// Light click for UI button taps:
window.AndroidEngine.hapticClick();

// Heavy vibration for explosions, critical damage, or major impacts:
window.AndroidEngine.hapticHeavy();

// Subtle tick for slot wheels, sliders, or rapid coin pickups:
window.AndroidEngine.hapticTick();

// Double click for level-ups, victories, or trophy unlocks:
window.AndroidEngine.hapticDouble();

// Custom duration pulse:
window.AndroidEngine.vibrate(200); // 200 milliseconds
```

---

## Hiding Web Top Menu, Header & Full Screen

When turning a responsive website into a mobile app or game, you often want to hide the website's top header/menu or make the screen immersive full-screen.

### 1. Hide Web Header / Top Menu (When You Don't Control the Site)
Pass `hideWebTopBar = true` in `WebViewConfig` or `QuickWebViewActivity`. It automatically injects CSS to hide `header`, `nav`, `.navbar`, `.top-bar`, `.site-header`, `#header`, and `#topbar`:

```kotlin
val config = WebViewConfig(
    url = "https://mywebsite.com",
    hideWebTopBar = true // Hides web top bar/navigation menu
)
```

You can also specify custom selectors or inject your own CSS:
```kotlin
val config = WebViewConfig(
    url = "https://mywebsite.com",
    hideWebTopBar = true,
    webTopBarSelectors = listOf(".my-custom-header", "#main-nav"),
    customCss = "#banner { display: none !important; }"
)
```

### 2. Immersive Full Screen (Hiding Android Status Bar)
For HTML5 games or full-screen apps, enable `fullScreen = true`:
```kotlin
val config = WebViewConfig(
    url = "https://mygame.com",
    fullScreen = true // Hides Android status bar and system navigation bars
)
```

### 3. Hide Top Menu from JavaScript on the Fly
The website frontend can also trigger hiding directly at runtime:
```javascript
// Hide default top header elements
window.AndroidEngine.hideTopBar();

// Or hide specific CSS selectors
window.AndroidEngine.hideTopBar(".my-header, #user-menu");

// Or inject any custom CSS
window.AndroidEngine.injectCss(".announcement-bar { display: none !important; }");
```

---

## Checking App Status & Diagnostics from Website (JavaScript Telemetry)

Your web application can inspect detailed native metadata, hardware capabilities, network status, battery level, permissions, and app review links directly through `window.AndroidEngine`.

### Automatic Page Injection
Every loaded web page is automatically injected with `window.__EISEN_APP_STATUS__` upon page load (`onPageFinished`), and dispatches an `eisen:appstatus` event:

```javascript
// Listen to the automatic injection event:
window.addEventListener("eisen:appstatus", (event) => {
    const status = event.detail;
    console.log("App Version:", status.app.versionName);
    console.log("Device:", status.device.manufacturer, status.device.model);
    console.log("Store:", status.app.installerStoreName);
    console.log("Battery:", status.battery.level + "%", status.battery.isCharging ? "(Charging)" : "");
});

// Or read the global variable directly:
if (window.__EISEN_APP_STATUS__) {
    console.log("Running in native app:", window.__EISEN_APP_STATUS__.app.appName);
}
```

### Direct Synchronous JavaScript Queries
You can also call individual telemetry methods anytime:

```javascript
// Full structured JSON payload
const fullStatus = JSON.parse(window.AndroidEngine.getAppStatus());

// Package name, version, installer store (Google Play, Huawei AppGallery, Sideload)
const appInfo = JSON.parse(window.AndroidEngine.getAppInfo());
console.log(appInfo.packageName, appInfo.versionName, appInfo.installerStoreName);

// Device specs: manufacturer, model, Android version, screen dimensions, tablet/tv detection
const deviceInfo = JSON.parse(window.AndroidEngine.getDeviceInfo());
console.log(deviceInfo.model, deviceInfo.androidVersion, deviceInfo.isTablet);

// Network state: isOnline, type (WIFI / CELLULAR / ETHERNET), isMetered
const network = JSON.parse(window.AndroidEngine.getNetworkStatus());
if (!network.isOnline) {
    showOfflineBanner();
}

// Battery state: percentage, isCharging, pluggedSource, isPowerSaveMode
const battery = JSON.parse(window.AndroidEngine.getBatteryStatus());

// Notification permission state
const perms = JSON.parse(window.AndroidEngine.getPermissionsStatus());
console.log("Notifications enabled:", perms.notificationsEnabled);

// In-App Review & Store Links
const reviewInfo = JSON.parse(window.AndroidEngine.getReviewInfo());
console.log("Store review URL:", reviewInfo.storeReviewUrl);

// Launch store review or direct listing page directly:
window.AndroidEngine.openStoreReviewPage();
```

---

## Google App Distribution & Google Play In-App Updates

The `:core-updater` module handles both pre-release tester distribution (Google Firebase App Distribution) and production updates (Google Play In-App Updates).

### 1. Google Play In-App Updates (Production)
Supports flexible (background download with user consent) and immediate (full-screen blocking) update flows:

```kotlin
val playUpdateManager = PlayUpdateManager(activity)

// Check if update is available:
playUpdateManager.checkForUpdate { info, error ->
    if (info?.isUpdateAvailable == true) {
        // Start Flexible update (allows user to keep playing while downloading)
        playUpdateManager.startUpdate(activity, immediate = false)
    }
}

// Listen for download completion and prompt restart:
playUpdateManager.setFlexibleUpdateListener { status ->
    if (status.isDownloaded) {
        playUpdateManager.completeUpdate()
    }
}
```

### 2. Google Firebase App Distribution (Pre-release & QA Testers)
Provides tester alerts when a new pre-release build is published in Firebase console, tester sign-in, and in-app screenshot feedback:

```kotlin
val appDistManager = AppDistributionManager(context)

// Check for new tester release and prompt:
appDistManager.updateIfNewReleaseAvailable(activity)

// Authenticate tester:
appDistManager.signInTester(activity) { success, error ->
    println("Tester signed in: $success")
}

// Open tester screenshot & bug feedback tool:
appDistManager.startFeedback("Found a bug on level 4")
```

### 3. JavaScript Updates Bridge (`window.AndroidUpdates`)
All update flows can be triggered directly by the web frontend:

```javascript
// Check Google Play update status:
window.AndroidUpdates.checkForPlayUpdate();
window.onPlayUpdateAvailable = (isAvailable, availableVersionCode, updatePriority) => {
    if (isAvailable) {
        showUpdateBanner("Update available! Tap to install.");
    }
};

// Start update (flexible or immediate):
window.AndroidUpdates.startPlayUpdate(false); // false = flexible, true = immediate

// Check Firebase App Distribution tester build:
window.AndroidUpdates.checkAppDistribution();

// Launch Firebase App Distribution in-app feedback:
window.AndroidUpdates.startDistributionFeedback("Visual glitch on shop screen");
```

---

## JavaScript Frontend API Summary

Your HTML5 website or game can trigger native functions directly:

```javascript
// Buy items in WebView
window.AndroidBilling.launchPurchase("coins_pack_100");

// Sign in with Firebase
window.AndroidAuth.signInAnonymously();

// Show rewarded ad
window.AndroidAds.showRewarded();
window.onRewardedAdWatched = (amount, type) => {
    console.log("Earned reward:", amount, type);
};

// Request In-App Review prompt
window.AndroidReview.requestReview();
window.onReviewCompleted = (success) => {
    console.log("Review completed:", success);
};

// Configure Bottom Navigation on the fly
window.AndroidEngine.setBottomNavItems(JSON.stringify([
    { id: "home", label: "Home", icon: "home", targetUrl: "https://mysite.com" },
    { id: "store", label: "Shop", icon: "store", targetUrl: "https://mysite.com/shop", badge: "NEW" },
    { id: "profile", label: "Profile", icon: "profile", targetUrl: "https://mysite.com/profile" }
]));

// Push Notifications: Query device push token (FCM or HMS)
window.AndroidPush.getPushToken();
window.onPushTokenReceived = (token, provider, isSuccess, error) => {
    console.log("Device Push Token:", token, "Provider:", provider);
    // Send token to your backend server
};

// Push Notifications: Subscribe / Unsubscribe from Topics
window.AndroidPush.subscribeToTopic("game_updates");

// Push Deep Link Handler in Frontend (when user taps notification)
window.onNotificationDeepLink = (path) => {
    console.log("Navigating to deep link path:", path); // e.g. "/store/coins"
    myAppRouter.navigate(path);
};

// Analytics: Log event (works on Firebase & Huawei HiAnalytics)
window.AndroidAnalytics.logEvent("level_end", JSON.stringify({
    level: 3,
    score: 2500,
    success: true
}));

// Analytics: Identify user & set user properties
window.AndroidAnalytics.setUserId("user_98765");
window.AndroidAnalytics.setUserProperty("plan", "vip");

// Game Services: Play Games & Huawei GameCenter
window.AndroidGames.signIn();
window.onGamesSignedIn = (success, player, error) => {
    console.log("Player name:", player?.displayName);
};
window.AndroidGames.submitScore("leaderboard_easy", 3500);
window.AndroidGames.showAllLeaderboards();
window.AndroidGames.unlockAchievement("achievement_pro");

// Viral Share: Text, Score & Screen Capture
window.AndroidShare.shareScore("Block Fusion", 4500, "https://play.google.com/store/apps/details?id=com.eisen.sample");
window.AndroidShare.captureAndShare("My High Score", "Check out my score on Block Fusion!");

// Biometrics: Fingerprint & Face Unlock
if (window.AndroidBiometrics && window.AndroidBiometrics.isAvailable()) {
    window.AndroidBiometrics.authenticate("Unlock Game Vault");
    window.onBiometricSuccess = () => console.log("Biometrics authenticated!");
    window.onBiometricFailed = (code, msg) => console.log("Biometrics failed:", msg);
}

// App Status & Hardware Telemetry
const appStatus = JSON.parse(window.AndroidEngine.getAppStatus());
console.log("Status:", appStatus.app.appName, appStatus.device.model, appStatus.battery.level + "%");

// Google Play In-App Updates & App Distribution
window.AndroidUpdates.checkForPlayUpdate();
window.onPlayUpdateAvailable = (isAvailable, versionCode) => {
    if (isAvailable) window.AndroidUpdates.startPlayUpdate(false);
};
window.AndroidUpdates.checkAppDistribution();
```
