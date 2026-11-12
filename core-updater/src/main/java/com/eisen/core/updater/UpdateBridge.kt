package com.eisen.core.updater

import android.app.Activity
import android.webkit.JavascriptInterface
import com.eisen.core.common.bridge.JsBridge
import org.json.JSONObject
import java.lang.ref.WeakReference

class UpdateBridge(
    activity: Activity,
    private val playUpdateManager: PlayUpdateManager? = null
) : JsBridge("AndroidUpdates") {

    private val activityRef = WeakReference(activity)

    @JavascriptInterface
    fun checkForPlayUpdate() {
        playUpdateManager?.checkForUpdate { status ->
            val json = playStatusToJson(status)
            evaluateJavascript("window.onPlayUpdateChecked && window.onPlayUpdateChecked($json)")
        }
    }

    @JavascriptInterface
    fun getPlayUpdateStatus(): String {
        val s = playUpdateManager?.status?.value ?: PlayUpdateStatus()
        return playStatusToJson(s)
    }

    @JavascriptInterface
    fun checkAppDistribution() {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            AppDistributionManager.updateIfNewReleaseAvailable { success ->
                evaluateJavascript("window.onAppDistributionChecked && window.onAppDistributionChecked($success)")
            }
        }
    }

    @JavascriptInterface
    fun signInDistributionTester() {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            AppDistributionManager.signInTester { success ->
                evaluateJavascript("window.onDistributionTesterSignedIn && window.onDistributionTesterSignedIn($success)")
            }
        }
    }

    @JavascriptInterface
    fun startDistributionFeedback(text: String? = null) {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            AppDistributionManager.startFeedback(text)
        }
    }

    @JavascriptInterface
    fun getAppDistributionStatus(): String {
        return JSONObject().apply {
            put("isTesterSignedIn", AppDistributionManager.isTesterSignedIn())
            put("isChecking", AppDistributionManager.status.value.isCheckingForUpdate)
            put("lastError", AppDistributionManager.status.value.lastError ?: "")
        }.toString()
    }

    private fun playStatusToJson(status: PlayUpdateStatus): String {
        return JSONObject().apply {
            put("isUpdateAvailable", status.isUpdateAvailable)
            put("availableVersionCode", status.availableVersionCode)
            put("stalenessDays", status.stalenessDays ?: -1)
            put("isImmediateAllowed", status.isImmediateAllowed)
            put("isFlexibleAllowed", status.isFlexibleAllowed)
            put("installStatus", status.installStatus)
            put("bytesDownloaded", status.bytesDownloaded)
            put("totalBytesToDownload", status.totalBytesToDownload)
            put("isDownloaded", status.isDownloaded)
        }.toString()
    }
}
