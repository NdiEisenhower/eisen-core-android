package com.eisen.core.review

import android.app.Activity
import android.content.Context

interface IReviewManager {
    /**
     * Request and launch the native in-app review dialog.
     * Completes with true if the flow completed, false otherwise.
     */
    fun launchReviewFlow(activity: Activity, onComplete: ((Boolean) -> Unit)? = null)

    /**
     * Directly opens the store details review page in Google Play Store or Huawei AppGallery.
     */
    fun openStoreReviewPage(context: Context)
}
