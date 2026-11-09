# Eisen Core Android 🚀

Modular, 100% Jetpack Compose Android SDK ecosystem designed for rapid app and HTML5 game development. No legacy XML views. Supports both **Google Play** and **Huawei AppGallery** out of the box with the latest 2026 APIs.

---

## 📦 Modules

| Module | Artifact | Description |
| :--- | :--- | :--- |
| **`core-webview`** | `:core-webview` | 100% Jetpack Compose turnkey WebView (`@Composable CoreWebView` + `QuickWebViewActivity`). File uploads, full-screen video, D-Pad/TV controls, Compose Material3 progress and error state, dynamic Bottom Navigation on the fly, and JS bridge registry. |
| **`core-billing`** | `:core-billing` | Unified In-App Purchases (**Google Play Billing 8.3** & **Huawei HMS IAP 6.13**) + WebView `window.AndroidBilling` bridge. |
| **`core-auth`** | `:core-auth` | Authentication (Google Sign-In, Firebase Auth, Guest) + WebView `window.AndroidAuth` bridge. |
| **`core-ads`** | `:core-ads` | Monetization (**Google AdMob 25.5** & **Huawei HMS Ads 13.4**) + WebView `window.AndroidAds` bridge. |
| **`core-review`** | `:core-review` | In-App Review API (**Google Play In-App Review 2.0.2** & **Huawei AppGallery In-App Comments**) + `window.AndroidReview` bridge. |
| **`core-updater`** | `:core-updater` | Jetpack Compose `ForceUpdateScreen` dialog + Remote Config version checking. |
| **`core-common`** | `:core-common` | Base `JsBridge`, `JsBridgeRegistry`, `CoreResult`, and Network monitoring. |

---

## 🏛️ Google Play & Huawei Dual-Flavor Architecture

Whether you build single-store apps or dual-store flavors (`google` and `huawei`), the SDK automatically detects the environment or lets you configure it explicitly via factories:

```kotlin
// 💳 Billing: Works on Google Play & Huawei
val billing = BillingFactory.create(context, store = BillingStore.AUTO) // or BillingStore.GOOGLE / BillingStore.HUAWEI
billing.initialize()

// 📺 Ads: Works on AdMob & Huawei Ads
val ads = AdFactory.create(context, store = AdStore.AUTO)
ads.initialize(activity)

// ⭐ In-App Review: Works on Google Play Review API & Huawei AppGallery Comments
val review = ReviewManagerFactory.create(context, flavor = StoreFlavor.AUTO, huaweiAppId = "123456789")
review.launchReviewFlow(activity)
```

---

## ⚡ Quick Start: Turnkey Web App in 60 Seconds

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
    // 🌐 100% Compose WebView
    implementation("com.github.NdiEisenhower.eisen-core-android:core-webview:v1.3.0")

    // 💳 Optional: In-App Purchases (Google + Huawei)
    implementation("com.github.NdiEisenhower.eisen-core-android:core-billing:v1.3.0")

    // 🔐 Optional: Login / Auth
    implementation("com.github.NdiEisenhower.eisen-core-android:core-auth:v1.3.0")

    // 📺 Optional: Ads (AdMob + Huawei Ads)
    implementation("com.github.NdiEisenhower.eisen-core-android:core-ads:v1.3.0")

    // ⭐ Optional: In-App Review API (Google Play + Huawei)
    implementation("com.github.NdiEisenhower.eisen-core-android:core-review:v1.3.0")

    // 🔄 Optional: Remote Force Update
    implementation("com.github.NdiEisenhower.eisen-core-android:core-updater:v1.3.0")
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
                    // 🧭 Define Bottom Navigation on the fly:
                    bottomNavItems = listOf(
                        BottomNavItem(id = "home", label = "Home", iconName = "home", targetUrl = "https://mycoolgame.com"),
                        BottomNavItem(id = "store", label = "Store", iconName = "store", targetUrl = "https://mycoolgame.com/store", badge = "SALE"),
                        BottomNavItem(id = "profile", label = "Profile", iconName = "profile", targetUrl = "https://mycoolgame.com/profile")
                    )
                )

                val state = rememberCoreWebViewState(config)
                
                // You can also add or change items dynamically at runtime:
                // state.addBottomNavItem(BottomNavItem(id = "chat", label = "Chat", iconName = "chat", badge = "3"))
                // state.setBadge("store", null)

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

## 🧩 Connecting Native Bridges (Payments, Auth, Ads, Reviews)

Extend `BaseWebViewActivity` (which is a Compose `ComponentActivity`) to attach native bridges:

```kotlin
class GameActivity : BaseWebViewActivity() {

    private lateinit var billingManager: IBillingManager
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var reviewManager: IReviewManager

    override fun getWebViewConfig(): WebViewConfig {
        return WebViewConfig(
            url = "https://mygame.com",
            enableDpadFocus = true,
            showProgressBar = true
        )
    }

    override fun onCreateBridges(registry: JsBridgeRegistry) {
        super.onCreateBridges(registry)

        // 1. Billing (Auto detects Google Play or Huawei)
        billingManager = BillingFactory.create(this).also { it.initialize() }
        registry.register(BillingBridge(this, billingManager))

        // 2. Auth (Firebase & Google Sign-in)
        authManager = FirebaseAuthManager()
        registry.register(AuthBridge(this, authManager))

        // 3. In-App Review (Google Play & Huawei AppGallery)
        reviewManager = ReviewManagerFactory.create(this, huaweiAppId = "109876543")
        registry.register(ReviewBridge(this, reviewManager))
    }

    override fun onDestroy() {
        billingManager.destroy()
        super.onDestroy()
    }
}
```

---

## 🌐 JavaScript Frontend API

Your HTML5 website or game can trigger native functions directly:

```javascript
// 💳 Buy items in WebView
window.AndroidBilling.launchPurchase("coins_pack_100");

// 🔐 Sign in with Firebase
window.AndroidAuth.signInAnonymously();

// 📺 Show rewarded ad
window.AndroidAds.showRewarded();
window.onRewardedAdWatched = (amount, type) => {
    console.log("Earned reward:", amount, type);
};

// ⭐ Request In-App Review prompt
window.AndroidReview.requestReview();
window.onReviewCompleted = (success) => {
    console.log("Review completed:", success);
};

// 🧭 Configure Bottom Navigation on the fly
window.AndroidEngine.setBottomNavItems(JSON.stringify([
    { id: "home", label: "Home", icon: "home", targetUrl: "https://mysite.com" },
    { id: "store", label: "Shop", icon: "store", targetUrl: "https://mysite.com/shop", badge: "NEW" },
    { id: "profile", label: "Profile", icon: "profile", targetUrl: "https://mysite.com/profile" }
]));
```
