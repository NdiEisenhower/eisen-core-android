# Eisen Core Android 🚀

Modular, plug-and-play Android SDK ecosystem designed for rapid app and HTML5 game development. Pick and choose only the components you need for each app.

---

## 📦 Modules

| Module | Artifact | Description |
| :--- | :--- | :--- |
| **`core-webview`** | `:core-webview` | Complete turnkey WebView shell. File uploads, full-screen video, D-Pad/TV controls, offline fallback, and JS bridge registry. |
| **`core-billing`** | `:core-billing` | Unified In-App Purchases (Google Play Billing + Huawei IAP) + WebView `window.AndroidBilling` bridge. |
| **`core-auth`** | `:core-auth` | Authentication (Google Sign-In, Firebase Auth, Guest) + WebView `window.AndroidAuth` bridge. |
| **`core-updater`** | `:core-updater` | Force Update & Remote Config version check with ready-to-use Compose UI. |
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
    // 🌐 Turnkey WebView
    implementation("com.github.NdiEisenhower.eisen-core-android:core-webview:1.0.0")

    // 💳 Optional: In-App Purchases
    implementation("com.github.NdiEisenhower.eisen-core-android:core-billing:1.0.0")

    // 🔐 Optional: Login / Auth
    implementation("com.github.NdiEisenhower.eisen-core-android:core-auth:1.0.0")

    // 🔄 Optional: Remote Force Update
    implementation("com.github.NdiEisenhower.eisen-core-android:core-updater:1.0.0")
}
```

### 3. Launch the App
In your `MainActivity.kt`:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🚀 ONE-LINER: Launches full app with upload support, TV navigation, & progress bar
        QuickWebViewActivity.launch(this, "https://yourwebsite.com")
        finish()
    }
}
```

---

## 🧩 Advanced Usage: Attaching Bridges to WebView

If your web game needs payments or login, extend `BaseWebViewActivity`:

```kotlin
class GameActivity : BaseWebViewActivity() {

    private lateinit var billingManager: GoogleBillingManager
    private lateinit var authManager: FirebaseAuthManager

    override fun getWebViewConfig(): WebViewConfig {
        return WebViewConfig(
            url = "https://blockrush2k.kharitambe.co.za",
            enableDpadFocus = true,
            showProgressBar = true,
            allowBackNavigation = true
        )
    }

    override fun onCreateBridges(registry: JsBridgeRegistry) {
        super.onCreateBridges(registry)

        // 1. Setup Billing & Bridge
        billingManager = GoogleBillingManager(this)
        billingManager.initialize()
        registry.register(BillingBridge(this, billingManager))

        // 2. Setup Auth & Bridge
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

## 🌐 JavaScript API in Your Web App

Once registered, your web frontend can call native methods directly:

### 1. Payments (`window.AndroidBilling`)
```javascript
// Check prices
window.AndroidBilling.queryProducts(JSON.stringify(["coins_100", "remove_ads"]));

// Listen for prices
window.onProductsAvailable = function(products) {
    console.log("Store prices loaded:", products);
};

// Buy an item
window.AndroidBilling.launchPurchase("coins_100");

// Listen for purchase result
window.onPurchaseSuccess = function(purchase) {
    console.log("Purchased successfully:", purchase.productId, purchase.purchaseToken);
    // Consume or acknowledge
    window.AndroidBilling.consumePurchase(purchase.purchaseToken);
};
```

### 2. Login (`window.AndroidAuth`)
```javascript
// Sign in anonymously / guest
window.AndroidAuth.signInAnonymously();

// Callback on auth
window.onAuthSuccess = function(user) {
    console.log("Logged in user:", user.uid);
};

// Check current user
const user = JSON.parse(window.AndroidAuth.getCurrentUser());
```

### 3. Core Engine (`window.AndroidEngine`)
```javascript
console.log("App Version:", window.AndroidEngine.getAppVersion());
console.log("Is Android TV:", window.AndroidEngine.isTvDevice());
window.AndroidEngine.vibrate(50);
window.AndroidEngine.showToast("Saved!");
```

---

## 🚀 Releasing a New Version

1. Commit your changes:
   ```bash
   git add .
   git commit -m "feat: Add new modules"
   ```
2. Push and tag a new release:
   ```bash
   git tag v1.0.0
   git push origin main --tags
   ```
3. JitPack will automatically build the artifacts, available at:
   `com.github.NdiEisenhower.eisen-core-android:<module-name>:<version>`

