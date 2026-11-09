package com.eisen.core.updater

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ForceUpdateManager {
    private const val TAG = "ForceUpdateManager"

    private const val DEFAULT_MIN_KEY = "min_required_version"
    private const val DEFAULT_LATEST_KEY = "latest_version"

    private val _updateInfo = MutableStateFlow(UpdateInfo())
    val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()

    private val _updateRequired = MutableStateFlow(false)
    val updateRequired: StateFlow<Boolean> = _updateRequired.asStateFlow()

    fun checkForUpdate(
        currentVersionCode: Int,
        minVersionKey: String = DEFAULT_MIN_KEY,
        latestVersionKey: String = DEFAULT_LATEST_KEY,
        customStoreUrl: String? = null,
        onResult: ((UpdateInfo) -> Unit)? = null
    ) {
        val remoteConfig = try {
            FirebaseRemoteConfig.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Remote Config is not initialized: ${e.message}")
            return
        }

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val minRequired = remoteConfig.getLong(minVersionKey).toInt()
                val rawLatest = remoteConfig.getLong(latestVersionKey).toInt()
                val latest = if (rawLatest == 0) minRequired else rawLatest

                val updateType = when {
                    currentVersionCode < minRequired -> UpdateType.HARD
                    currentVersionCode < latest -> UpdateType.SOFT
                    else -> UpdateType.NONE
                }

                val info = UpdateInfo(
                    type = updateType,
                    latestVersionCode = latest,
                    minRequiredVersionCode = minRequired,
                    storeUrl = customStoreUrl
                )

                _updateInfo.value = info
                _updateRequired.value = (updateType == UpdateType.HARD)

                Log.d(TAG, "Update check: current=$currentVersionCode, min=$minRequired, latest=$latest, type=$updateType")
                onResult?.invoke(info)
            } else {
                Log.w(TAG, "RemoteConfig fetch failed: ${task.exception?.message}")
            }
        }
    }
}

