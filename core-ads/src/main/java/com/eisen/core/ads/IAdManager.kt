package com.eisen.core.ads

import android.app.Activity
import android.view.ViewGroup

interface AdRewardListener {
    fun onUserEarnedReward(type: String, amount: Int)
    fun onAdClosed()
    fun onAdFailedToLoad(error: String)
}

interface IAdManager {
    fun initialize(activity: Activity, onInitialized: (() -> Unit)? = null)
    fun loadBanner(activity: Activity, adUnitId: String, container: ViewGroup)
    fun hideBanner(container: ViewGroup)
    fun loadInterstitial(activity: Activity, adUnitId: String)
    fun showInterstitial(activity: Activity, onDismissed: (() -> Unit)? = null)
    fun loadRewarded(activity: Activity, adUnitId: String)
    fun showRewarded(activity: Activity, listener: AdRewardListener)
}

