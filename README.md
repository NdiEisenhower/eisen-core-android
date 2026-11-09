# Eisen Core Android 🚀

Modular, 100% Jetpack Compose Android SDK ecosystem designed for rapid app and HTML5 game development. No legacy XML views. Pick and choose only the components you need for each app.

---

## 📦 Modules

| Module | Artifact | Description |
| :--- | :--- | :--- |
| **`core-webview`** | `:core-webview` | 100% Jetpack Compose turnkey WebView (`@Composable CoreWebView` + `QuickWebViewActivity`). File uploads, full-screen video, D-Pad/TV controls, Compose Material3 progress and error state, and JS bridge registry. |
| **`core-billing`** | `:core-billing` | Unified In-App Purchases (Google Play Billing + Huawei IAP) + WebView `window.AndroidBilling` bridge. |
| **`core-auth`** | `:core-auth` | Authentication (Google Sign-In, Firebase Auth, Guest) + WebView `window.AndroidAuth` bridge. |
| **`core-ads`** | `:core-ads` | AdMob + Huawei Ads monetization + WebView `window.AndroidAds` bridge. |
| **`core-updater`** | `:core-updater` | Jetpack Compose `ForceUpdateScreen` dialog + Remote Config version checking. |
| **`core-common`** | `:core-common` | Base `JsBridge`, `JsBridgeRegistry`, `CoreResult`, and Network monitoring. |

---

## ⚡ Quick Start: Turnkey Web App in 60 Seconds

### 1. Add Repository
In your new app's `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

### 2. Add Desired Dependencies
In your new app's `app/build.gradle.kts`:
```kotlin
dependencies {
    // 🌐 100% Compose WebView
    implementation("com.github.NdiEisenhower.eisen-core-android:core-webview:v1.1.0")

    // 💳 Optional: In-App Purchases
    implementation("com.github.NdiEisenhower.eisen-core-android:core-billing:v1.1.0")

    // 🔐 Optional: Login / Auth
    implementation("com.github.NdiEisenhower.eisen-core-android:core-auth:v1.1.0")

    // 📺 Optional: Ads
    implementation("com.github.NdiEisenhower.eisen-core-android:core-ads:v1.1.0")

    // 🔄 Optional: Remote Force Update
    implementation("com.github.NdiEisenhower.eisen-core-android:core-updater:v1.1.0")
}
```

### 3. Option A: Pure Jetpack Compose Screen
Drop `@Composable CoreWebView` directly into any Compose layout or Scaffold without leaving Compose. You can also define **Bottom Navigation on the fly**:

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

---

## 🧭 Managing Bottom Navigation from JavaScript (On the fly!)

Your web application can configure, add, or change native Android Bottom Navigation items in real-time directly through JavaScript:

```javascript
// Add or replace bottom navigation tabs dynamically:
window.AndroidEngine.setBottomNavItems(JSON.stringify([
    { id: "home", label: "Home", icon: "home", targetUrl: "https://mysite.com" },
    { id: "store", label: "Shop", icon: "store", targetUrl: "https://mysite.com/shop", badge: "HOT" },
    { id: "profile", label: "Profile", icon: "profile", targetUrl: "https://mysite.com/profile" }
]));

// Update badge on a tab:
window.AndroidEngine.setBottomNavBadge("store", "2");

// Switch tab programmatically:
window.AndroidEngine.selectBottomNavItem("store");

// Listen when user clicks a bottom nav tab in Android:
window.onBottomNavSelected = function(tabId) {
    console.log("User tapped tab:", tabId);
    // Handle SPA routing or view switching in JS
};
```

---

## 🧩 Connecting Native Bridges (Payments, Auth, Ads)

Extend `BaseWebViewActivity` (which is a Compose `ComponentActivity`) to attach native bridges:

```kotlin
class GameActivity : BaseWebViewActivity() {

    private lateinit var billingManager: GoogleBillingManager
    private lateinit var authManager: FirebaseAuthManager

    override fun getWebViewConfig(): WebViewConfig {
        return WebViewConfig(
            url = "https://mygame.com",
            enableDpadFocus = true,
            showProgressBar = true
        )
    }

    override fun onCreateBridges(registry: JsBridgeRegistry) {
        super.onCreateBridges(registry)

        // Attach billing bridge
        billingManager = GoogleBillingManager(this).also { it.initialize() }
        registry.register(BillingBridge(this, billingManager))

        // Attach auth bridge
        authManager = FirebaseAuthManager()
        registry.register(AuthBridge(this, authManager))
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
// Buy items in WebView
window.AndroidBilling.launchPurchase("coins_pack_100");

// Sign in with Firebase
window.AndroidAuth.signInAnonymously();

// Show rewarded ad
window.AndroidAds.showRewarded();
window.onRewardedAdWatched = (amount, type) => {
    console.log("Earned reward:", amount, type);
};
```
