package com.eisen.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import com.eisen.core.ads.AdFactory
import com.eisen.core.ads.AdRewardListener
import com.eisen.core.auth.FirebaseAuthManager
import com.eisen.core.billing.BillingFactory
import com.eisen.core.common.bridge.JsBridgeRegistry
import com.eisen.core.review.ReviewManagerFactory
import com.eisen.core.updater.UpdateInfo
import com.eisen.core.updater.UpdateType
import com.eisen.core.updater.ui.ForceUpdateScreen
import com.eisen.core.webview.WebViewConfig
import com.eisen.core.webview.model.BottomNavItem
import com.eisen.core.webview.ui.CoreWebView
import com.eisen.core.webview.ui.rememberCoreWebViewState

class MainActivity : ComponentActivity() {

    private val billingManager by lazy { BillingFactory.create(this) }
    private val authManager by lazy { FirebaseAuthManager() }
    private val adManager by lazy { AdFactory.create(this) }
    private val reviewManager by lazy { ReviewManagerFactory.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingManager.initialize()
        adManager.initialize(this)

        setContent {
            MaterialTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                var showUpdateDialog by remember { mutableStateOf(false) }

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

                                    // 1. In-App Review
                                    Button(
                                        onClick = {
                                            reviewManager.launchReviewFlow(this@MainActivity) { success ->
                                                Toast.makeText(this@MainActivity, "Review flow result: $success", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("⭐ Test In-App Review API")
                                    }

                                    // 2. Billing
                                    Button(
                                        onClick = {
                                            billingManager.queryProducts(listOf("coins_100", "remove_ads"))
                                            Toast.makeText(this@MainActivity, "Querying products...", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("💳 Test In-App Purchases (Query)")
                                    }

                                    // 3. Ads
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
                                        Text("📺 Test Rewarded Ad")
                                    }

                                    // 4. Force Update Screen
                                    Button(
                                        onClick = { showUpdateDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("🔄 Preview Force Update Dialog")
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
        super.onDestroy()
    }
}

