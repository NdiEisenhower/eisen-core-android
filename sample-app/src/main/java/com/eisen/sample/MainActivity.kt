package com.eisen.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.eisen.core.ads.AdFactory
import com.eisen.core.ads.AdRewardListener
import com.eisen.core.analytics.AnalyticsFactory
import com.eisen.core.analytics.model.CoreAnalyticsEvent
import com.eisen.core.auth.FirebaseAuthManager
import com.eisen.core.billing.BillingFactory
import com.eisen.core.games.GameFactory
import com.eisen.core.notifications.PushFactory
import com.eisen.core.notifications.channel.NotificationChannelHelper
import com.eisen.core.notifications.model.NotificationPayload
import com.eisen.core.permissions.ui.NotificationPermissionRequester
import com.eisen.core.review.ReviewManagerFactory
import com.eisen.core.security.BiometricHelper
import com.eisen.core.share.ShareHelper
import com.eisen.core.updater.UpdateInfo
import com.eisen.core.updater.UpdateType
import com.eisen.core.updater.ui.ForceUpdateScreen
import com.eisen.core.webview.WebViewConfig
import com.eisen.core.webview.model.BottomNavItem
import com.eisen.core.webview.ui.CoreWebView

class MainActivity : FragmentActivity() {

    private val billingManager by lazy { BillingFactory.create(this) }
    private val authManager by lazy { FirebaseAuthManager() }
    private val adManager by lazy { AdFactory.create(this) }
    private val reviewManager by lazy { ReviewManagerFactory.create(this) }
    private val pushManager by lazy { PushFactory.create(this) }
    private val analyticsManager by lazy { AnalyticsFactory.create(this) }
    private val gameManager by lazy { GameFactory.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingManager.initialize()
        adManager.initialize(this)
        pushManager.initialize(this)
        gameManager.initialize(this)

        setContent {
            MaterialTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                var showUpdateDialog by remember { mutableStateOf(false) }

                // Automatically check / request Notification permission on Android 13+
                NotificationPermissionRequester(
                    onResult = { granted ->
                        if (!granted) {
                            Toast.makeText(this@MainActivity, "Notifications not granted", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Public, "Web") },
                                label = { Text("Web View") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.Dashboard, "SDK Features") },
                                label = { Text("SDK Features") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (selectedTab) {
                            0 -> {
                                val config = WebViewConfig(
                                    url = "https://blockrush2k.kharitambe.co.za",
                                    enableDpadFocus = true,
                                    showProgressBar = true,
                                    bottomNavItems = listOf(
                                        BottomNavItem("game", "Game", "play"),
                                        BottomNavItem("store", "Store", "store", badge = "NEW"),
                                        BottomNavItem("profile", "Profile", "profile")
                                    )
                                )
                                CoreWebView(config = config)
                            }
                            1 -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Eisen Core SDK Tester",
                                        style = MaterialTheme.typography.headlineMedium
                                    )

                                    // 1. Game Services (Google Play Games & Huawei Game)
                                    Button(
                                        onClick = {
                                            gameManager.signIn(this@MainActivity) { success, player, error ->
                                                val msg = if (success) "Signed in as ${player?.displayName}" else "Sign-in error: $error"
                                                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Test Play Games / Huawei Game Sign-In")
                                    }

                                    // 2. Share & Screenshot
                                    Button(
                                        onClick = {
                                            ShareHelper.shareScore(
                                                this@MainActivity,
                                                gameName = "Block Fusion",
                                                score = 4500,
                                                storeUrl = "https://play.google.com/store/apps/details?id=com.eisen.sample"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Test Viral Score Share")
                                    }

                                    Button(
                                        onClick = {
                                            ShareHelper.captureAndShare(
                                                this@MainActivity,
                                                title = "Share Screen",
                                                message = "Look at my game screenshot!"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Capture Screenshot & Share")
                                    }

                                    // 3. Biometrics
                                    Button(
                                        onClick = {
                                            if (BiometricHelper.isBiometricAvailable(this@MainActivity)) {
                                                BiometricHelper.authenticate(
                                                    activity = this@MainActivity,
                                                    title = "Unlock Game Vault",
                                                    onSuccess = {
                                                        Toast.makeText(this@MainActivity, "Biometrics Success!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onError = { code, err ->
                                                        Toast.makeText(this@MainActivity, "Biometrics Error $code: $err", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            } else {
                                                Toast.makeText(this@MainActivity, "Biometrics not available on device", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Test Biometric Fingerprint / Face Unlock")
                                    }

                                    // 4. Push Notifications
                                    Button(
                                        onClick = {
                                            pushManager.getPushToken { result ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Push [${result.provider}]: ${result.token?.take(15)}...",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Test Push Token")
                                    }

                                    Button(
                                        onClick = {
                                            NotificationChannelHelper.showNotification(
                                                this@MainActivity,
                                                NotificationPayload(
                                                    title = "Special Offer!",
                                                    body = "Tap to claim 100 free coins in Store",
                                                    deepLink = "/store"
                                                ),
                                                MainActivity::class.java
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Test Deep Link Notification")
                                    }

                                    // 5. Analytics
                                    Button(
                                        onClick = {
                                            analyticsManager.logEvent(
                                                CoreAnalyticsEvent.LEVEL_START,
                                                mapOf(
                                                    CoreAnalyticsEvent.Param.LEVEL to 1,
                                                    CoreAnalyticsEvent.Param.LEVEL_NAME to "Tutorial"
                                                )
                                            )
                                            Toast.makeText(this@MainActivity, "Logged 'level_start' event!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Test Analytics Event")
                                    }

                                    // 6. In-App Review
                                    Button(
                                        onClick = {
                                            reviewManager.launchReviewFlow(this@MainActivity) { success ->
                                                Toast.makeText(this@MainActivity, "Review flow result: $success", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Test In-App Review API")
                                    }

                                    // 7. Billing
                                    Button(
                                        onClick = {
                                            billingManager.queryProducts(listOf("coins_100", "remove_ads"))
                                            Toast.makeText(this@MainActivity, "Querying products...", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Test In-App Purchases (Query)")
                                    }

                                    // 8. Ads
                                    Button(
                                        onClick = {
                                            adManager.showRewarded(this@MainActivity, object : AdRewardListener {
                                                override fun onUserEarnedReward(type: String, amount: Int) {
                                                    Toast.makeText(this@MainActivity, "Rewarded: $amount $type", Toast.LENGTH_LONG).show()
                                                }
                                                override fun onAdClosed() {}
                                                override fun onAdFailedToLoad(error: String) {
                                                    Toast.makeText(this@MainActivity, "Ad error: $error", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Test Rewarded Ad")
                                    }

                                    // 9. Force Update Screen
                                    Button(
                                        onClick = { showUpdateDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Preview Force Update Dialog")
                                    }
                                }
                            }
                        }

                        if (showUpdateDialog) {
                            ForceUpdateScreen(
                                appName = "Sample Game",
                                updateInfo = UpdateInfo(type = UpdateType.HARD, latestVersionCode = 2, minRequiredVersionCode = 2),
                                onUpdateClick = {
                                    showUpdateDialog = false
                                    Toast.makeText(this@MainActivity, "Update clicked!", Toast.LENGTH_SHORT).show()
                                },
                                onDismissClick = { showUpdateDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        billingManager.destroy()
        pushManager.destroy()
        super.onDestroy()
    }
}
