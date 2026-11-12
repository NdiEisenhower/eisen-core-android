package com.eisen.core.updater

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayUpdateStatus(
    val isUpdateAvailable: Boolean = false,
    val availableVersionCode: Int = 0,
    val stalenessDays: Int? = null,
    val isImmediateAllowed: Boolean = false,
    val isFlexibleAllowed: Boolean = false,
    val installStatus: String = "UNKNOWN",
    val bytesDownloaded: Long = 0L,
    val totalBytesToDownload: Long = 0L,
    val isDownloaded: Boolean = false
)

/**
 * Google Play In-App Updates Manager.
 * Supports flexible and immediate in-app updates directly from Google Play.
 */
class PlayUpdateManager(private val context: Context) {

    companion object {
        private const val TAG = "PlayUpdateManager"
    }

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var cachedAppUpdateInfo: AppUpdateInfo? = null

    private val _status = MutableStateFlow(PlayUpdateStatus())
    val status: StateFlow<PlayUpdateStatus> = _status.asStateFlow()

    private val installStateListener = InstallStateUpdatedListener { state ->
        val statusName = when (state.installStatus()) {
            InstallStatus.PENDING -> "PENDING"
            InstallStatus.DOWNLOADING -> "DOWNLOADING"
            InstallStatus.DOWNLOADED -> "DOWNLOADED"
            InstallStatus.INSTALLING -> "INSTALLING"
            InstallStatus.INSTALLED -> "INSTALLED"
            InstallStatus.FAILED -> "FAILED"
            InstallStatus.CANCELED -> "CANCELED"
            else -> "UNKNOWN"
        }

        val isDownloaded = state.installStatus() == InstallStatus.DOWNLOADED
        _status.value = _status.value.copy(
            installStatus = statusName,
            bytesDownloaded = state.bytesDownloaded(),
            totalBytesToDownload = state.totalBytesToDownload(),
            isDownloaded = isDownloaded
        )

        Log.d(TAG, "Install state: $statusName (${state.bytesDownloaded()} / ${state.totalBytesToDownload()})")
    }

    init {
        try {
            appUpdateManager.registerListener(installStateListener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register install state listener: ${e.message}")
        }
    }

    /**
     * Checks Google Play for in-app updates.
     */
    fun checkForUpdate(onResult: ((PlayUpdateStatus) -> Unit)? = null) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                cachedAppUpdateInfo = info
                val isAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                        info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

                val playStatus = PlayUpdateStatus(
                    isUpdateAvailable = isAvailable,
                    availableVersionCode = info.availableVersionCode(),
                    stalenessDays = info.clientVersionStalenessDays(),
                    isImmediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE),
                    isFlexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
                    installStatus = if (info.installStatus() == InstallStatus.DOWNLOADED) "DOWNLOADED" else "READY"
                )

                _status.value = playStatus
                Log.d(TAG, "Play update check: available=$isAvailable, version=${info.availableVersionCode()}")
                onResult?.invoke(playStatus)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to check for Google Play updates: ${e.message}")
                onResult?.invoke(_status.value)
            }
    }

    /**
     * Starts the in-app update flow using ActivityResultLauncher.
     */
    fun startUpdate(
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        immediate: Boolean = false
    ): Boolean {
        val info = cachedAppUpdateInfo ?: return false
        val updateType = if (immediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE

        return if (info.isUpdateTypeAllowed(updateType)) {
            val options = AppUpdateOptions.defaultOptions(updateType)
            appUpdateManager.startUpdateFlowForResult(info, launcher, options)
            true
        } else {
            Log.w(TAG, "Update type $updateType is not allowed for this update")
            false
        }
    }

    /**
     * Completes a flexible update after download finishes, restarting the app.
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun destroy() {
        try {
            appUpdateManager.unregisterListener(installStateListener)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering installStateListener: ${e.message}")
        }
    }
}
