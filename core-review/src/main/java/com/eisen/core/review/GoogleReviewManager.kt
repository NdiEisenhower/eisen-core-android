package com.eisen.core.review

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

class GoogleReviewManager : IReviewManager {

    companion object {
        private const val TAG = "GoogleReviewManager"
    }

    override fun launchReviewFlow(activity: Activity, onComplete: ((Boolean) -> Unit)?) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    Log.d(TAG, "Google in-app review flow completed.")
                    onComplete?.invoke(true)
                }
            } else {
                Log.w(TAG, "Failed to request review flow: ${task.exception?.message}. Falling back to Play Store page.")
                openStoreReviewPage(activity)
                onComplete?.invoke(false)
            }
        }
    }

    override fun openStoreReviewPage(context: Context) {
        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
