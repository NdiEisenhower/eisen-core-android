package com.eisen.core.ads

import android.app.Activity
import android.webkit.JavascriptInterface
import com.eisen.core.common.bridge.JsBridge
import java.lang.ref.WeakReference

class AdBridge(
    activity: Activity,
    private val adManager: IAdManager
) : JsBridge("AndroidAds") {

    private val activityRef = WeakReference(activity)

    @JavascriptInterface
    fun loadInterstitial(adUnitId: String) {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            adManager.loadInterstitial(activity, adUnitId)
        }
    }

    @JavascriptInterface
    fun showInterstitial() {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            adManager.showInterstitial(activity) {
                evaluateJavascript("window.onInterstitialClosed && window.onInterstitialClosed()")
            }
        }
    }

    @JavascriptInterface
    fun loadRewarded(adUnitId: String) {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            adManager.loadRewarded(activity, adUnitId)
        }
    }

    @JavascriptInterface
    fun showRewarded() {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            adManager.showRewarded(activity, object : AdRewardListener {
                override fun onUserEarnedReward(type: String, amount: Int) {
                    evaluateJavascript("window.onRewardedAdWatched && window.onRewardedAdWatched($amount, '$type')")
                }

                override fun onAdClosed() {
                    evaluateJavascript("window.onRewardedAdClosed && window.onRewardedAdClosed()")
                }

                override fun onAdFailedToLoad(error: String) {
                    evaluateJavascript("window.onRewardedAdFailed && window.onRewardedAdFailed('$error')")
                }
            })
        }
    }
}
