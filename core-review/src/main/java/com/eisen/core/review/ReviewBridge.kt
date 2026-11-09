package com.eisen.core.review

import android.app.Activity
import android.webkit.JavascriptInterface
import com.eisen.core.common.bridge.JsBridge
import java.lang.ref.WeakReference

class ReviewBridge(
    activity: Activity,
    private val reviewManager: IReviewManager
) : JsBridge("AndroidReview") {

    private val activityRef = WeakReference(activity)

    @JavascriptInterface
    fun requestReview() {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            reviewManager.launchReviewFlow(activity) { success ->
                evaluateJavascript("window.onReviewCompleted && window.onReviewCompleted($success)")
            }
        }
    }

    @JavascriptInterface
    fun openStoreReviewPage() {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            reviewManager.openStoreReviewPage(activity)
        }
    }
}
