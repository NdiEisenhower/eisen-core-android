package com.eisen.core.ads

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import com.huawei.hms.ads.*
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.ads.reward.Reward
import com.huawei.hms.ads.reward.RewardAd
import com.huawei.hms.ads.reward.RewardAdLoadListener
import com.huawei.hms.ads.reward.RewardAdStatusListener

class HuaweiAdsManager : IAdManager {

    companion object {
        private const val TAG = "HuaweiAdsManager"
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardAd: RewardAd? = null
    private var bannerView: BannerView? = null
    private var isInitialized = false

    override fun initialize(activity: Activity, onInitialized: (() -> Unit)?) {
        if (isInitialized) {
            onInitialized?.invoke()
            return
        }
        HwAds.init(activity)
        isInitialized = true
        Log.d(TAG, "Huawei Ads initialized")
        onInitialized?.invoke()
    }

    override fun loadBanner(activity: Activity, adUnitId: String, container: ViewGroup) {
        bannerView?.let {
            container.removeView(it)
            it.destroy()
        }

        val adView = BannerView(activity).apply {
            bannerAdSize = BannerAdSize.BANNER_SIZE_320_50
            adId = adUnitId
        }
        bannerView = adView
        container.removeAllViews()
        container.addView(adView)

        val adParam = AdParam.Builder().build()
        adView.loadAd(adParam)
    }

    override fun hideBanner(container: ViewGroup) {
        bannerView?.let {
            container.removeView(it)
            it.destroy()
            bannerView = null
        }
    }

    override fun loadInterstitial(activity: Activity, adUnitId: String) {
        val ad = InterstitialAd(activity).apply {
            this.adId = adUnitId
        }
        interstitialAd = ad
        val adParam = AdParam.Builder().build()
        ad.loadAd(adParam)
    }

    override fun showInterstitial(activity: Activity, onDismissed: (() -> Unit)?) {
        val ad = interstitialAd
        if (ad != null && ad.isLoaded) {
            ad.adListener = object : AdListener() {
                override fun onAdClosed() {
                    interstitialAd = null
                    onDismissed?.invoke()
                }

                override fun onAdFailed(errorCode: Int) {
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
        val ad = RewardAd(activity, adUnitId)
        rewardAd = ad
        val adParam = AdParam.Builder().build()
        ad.loadAd(adParam, object : RewardAdLoadListener() {
            override fun onRewardAdLoaded() {
                Log.d(TAG, "Huawei Reward ad loaded")
            }

            override fun onRewardAdFailedToLoad(errorCode: Int) {
                rewardAd = null
                Log.w(TAG, "Huawei Reward ad failed: $errorCode")
            }
        })
    }

    override fun showRewarded(activity: Activity, listener: AdRewardListener) {
        val ad = rewardAd
        if (ad != null && ad.isLoaded) {
            ad.show(activity, object : RewardAdStatusListener() {
                override fun onRewardAdClosed() {
                    rewardAd = null
                    listener.onAdClosed()
                }

                override fun onRewardAdFailedToShow(errorCode: Int) {
                    rewardAd = null
                    listener.onAdFailedToLoad("Failed with code: $errorCode")
                }

                override fun onRewarded(reward: Reward) {
                    listener.onUserEarnedReward(reward.name ?: "reward", reward.amount)
                }
            })
        } else {
            listener.onAdFailedToLoad("Huawei reward ad not ready")
        }
    }
}

