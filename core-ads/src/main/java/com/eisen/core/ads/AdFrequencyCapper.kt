package com.eisen.core.ads

import android.app.Activity
import android.os.SystemClock

class AdFrequencyCapper(
    var minCooldownSeconds: Long = 60L
) {
    private var lastInterstitialTimeMs: Long = 0L

    fun canShowInterstitial(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val elapsed = (now - lastInterstitialTimeMs) / 1000
        return elapsed >= minCooldownSeconds
    }

    fun recordInterstitialShown() {
        lastInterstitialTimeMs = SystemClock.elapsedRealtime()
    }

    fun showInterstitialIfAllowed(
        adManager: IAdManager,
        activity: Activity,
        onDismissed: (() -> Unit)? = null
    ): Boolean {
        return if (canShowInterstitial()) {
            recordInterstitialShown()
            adManager.showInterstitial(activity, onDismissed)
            true
        } else {
            false
        }
    }

    fun remainingCooldownSeconds(): Long {
        val now = SystemClock.elapsedRealtime()
        val elapsed = (now - lastInterstitialTimeMs) / 1000
        val remaining = minCooldownSeconds - elapsed
        return if (remaining > 0) remaining else 0L
    }

    fun reset() {
        lastInterstitialTimeMs = 0L
    }
}

