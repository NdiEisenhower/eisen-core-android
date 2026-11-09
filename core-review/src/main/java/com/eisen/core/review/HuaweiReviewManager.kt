package com.eisen.core.review

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class HuaweiReviewManager(
    private val huaweiAppId: String? = null
) : IReviewManager {

    companion object {
        private const val TAG = "HuaweiReviewManager"
        private const val ACTION_COMMENTS = "com.huawei.appmarket.appmarket.intent.action.AppDetailWithComments"
    }

    override fun launchReviewFlow(activity: Activity, onComplete: ((Boolean) -> Unit)?) {
        try {
            // Launch Huawei in-app comment dialog intent
            val intent = Intent(ACTION_COMMENTS).apply {
                setPackage("com.huawei.appmarket")
                if (!huaweiAppId.isNullOrEmpty()) {
                    putExtra("appId", huaweiAppId)
                }
            }
            if (activity.packageManager.resolveActivity(intent, 0) != null) {
                activity.startActivity(intent)
                onComplete?.invoke(true)
                return
            }
        } catch (e: Exception) {
            Log.w(TAG, "Huawei comments action failed: ${e.message}")
        }

        // Fallback to AppGallery store listing
        openStoreReviewPage(activity)
        onComplete?.invoke(true)
    }

    override fun openStoreReviewPage(context: Context) {
        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("appmarket://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://appgallery.huawei.com/app/C$huaweiAppId")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}

