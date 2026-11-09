package com.eisen.core.webview

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.eisen.core.common.bridge.JsBridge
import com.eisen.core.webview.model.BottomNavItem
import org.json.JSONArray
import java.util.concurrent.CopyOnWriteArrayList

interface EngineEventListener {
    fun onUpdateBottomNavItems(items: List<BottomNavItem>) {}
    fun onSelectBottomNavItem(id: String) {}
    fun onUpdateBadge(id: String, badge: String?) {}
    fun onToggleBottomNav(visible: Boolean) {}
    fun onRequestReview() {}
}

class EngineJsBridge(
    private val activity: Activity
) : JsBridge("AndroidEngine") {

    private val listeners = CopyOnWriteArrayList<EngineEventListener>()

    fun addListener(listener: EngineEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EngineEventListener) {
        listeners.remove(listener)
    }

    @JavascriptInterface
    fun getAppVersion(): String {
        return try {
            val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    @JavascriptInterface
    fun getPackageName(): String = activity.packageName

    @JavascriptInterface
    fun isTvDevice(): Boolean {
        val uiModeManager = activity.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    @JavascriptInterface
    fun openExternal(url: String) {
        activity.runOnUiThread {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        activity.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun requestReview() {
        activity.runOnUiThread {
            listeners.forEach { it.onRequestReview() }
        }
    }

    @JavascriptInterface
    fun vibrate(durationMs: Long) {
        try {
            val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Add or replace bottom navigation items on the fly from JavaScript!
     * Format: [{"id":"home","label":"Home","icon":"home","targetUrl":"https://...","badge":"2"}]
     */
    @JavascriptInterface
    fun setBottomNavItems(itemsJson: String) {
        activity.runOnUiThread {
            try {
                val jsonArray = JSONArray(itemsJson)
                val list = mutableListOf<BottomNavItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        BottomNavItem(
                            id = obj.optString("id", "item_$i"),
                            label = obj.optString("label", ""),
                            iconName = obj.optString("icon", "home"),
                            targetUrl = if (obj.has("targetUrl")) obj.getString("targetUrl") else null,
                            badge = if (obj.has("badge")) obj.getString("badge") else null
                        )
                    )
                }
                listeners.forEach { it.onUpdateBottomNavItems(list) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun selectBottomNavItem(id: String) {
        activity.runOnUiThread {
            listeners.forEach { it.onSelectBottomNavItem(id) }
        }
    }

    @JavascriptInterface
    fun setBottomNavBadge(id: String, badge: String) {
        activity.runOnUiThread {
            listeners.forEach { it.onUpdateBadge(id, badge.ifEmpty { null }) }
        }
    }

    @JavascriptInterface
    fun hideBottomNav() {
        activity.runOnUiThread {
            listeners.forEach { it.onToggleBottomNav(false) }
        }
    }

    @JavascriptInterface
    fun showBottomNav() {
        activity.runOnUiThread {
            listeners.forEach { it.onToggleBottomNav(true) }
        }
    }
}
