package com.eisen.core.ads

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobManager : IAdManager {

    companion object {
        private const val TAG = "AdMobManager"
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var bannerView: AdView? = null
    private var isInitialized = false

    override fun initialize(activity: Activity, onInitialized: (() -> Unit)?) {
        if (isInitialized) {
            onInitialized?.invoke()
            return
        }
        MobileAds.initialize(activity) {
            isInitialized = true
            Log.d(TAG, "AdMob initialized")
            onInitialized?.invoke()
        }
    }

    override fun loadBanner(activity: Activity, adUnitId: String, container: ViewGroup) {
        bannerView?.let {
            container.removeView(it)
            it.destroy()
        }

        val adView = AdView(activity).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
        }
        bannerView = adView
        container.removeAllViews()
        container.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun hideBanner(container: ViewGroup) {
        bannerView?.let {
            container.removeView(it)
            it.destroy()
            bannerView = null
        }
    }

    override fun loadInterstitial(activity: Activity, adUnitId: String) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.d(TAG, "Interstitial ad loaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                interstitialAd = null
                Log.w(TAG, "Failed to load interstitial: ${loadAdError.message}")
            }
        })
    }

    override fun showInterstitial(activity: Activity, onDismissed: (() -> Unit)?) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    onDismissed?.invoke()
                }
            }
            ad.show(activity)
        } else {
            onDismissed?.invoke()
        }
    }

    override fun loadRewarded(activity: Activity, adUnitId: String) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(activity, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                Log.d(TAG, "Rewarded ad loaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                rewardedAd = null
                Log.w(TAG, "Failed to load rewarded ad: ${loadAdError.message}")
            }
        })
    }

    override fun showRewarded(activity: Activity, listener: AdRewardListener) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    listener.onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    listener.onAdFailedToLoad(adError.message)
                }
            }
            ad.show(activity) { rewardItem ->
                listener.onUserEarnedReward(rewardItem.type, rewardItem.amount)
            }
        } else {
            listener.onAdFailedToLoad("Rewarded ad not ready")
        }
    }
}
