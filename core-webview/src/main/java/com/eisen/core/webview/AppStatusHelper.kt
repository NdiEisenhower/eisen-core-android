package com.eisen.core.webview

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.pm.PackageInfoCompat
import org.json.JSONObject
import java.util.Locale
import java.util.TimeZone

/**
 * Utility helper to gather complete status, metadata, hardware specs,
 * review URLs, and device telemetry for consumption by HTML5 websites and JS games.
 */
object AppStatusHelper {

    /**
     * Returns a complete, unified JSON representation of the app, device, network,
     * battery, permissions, and store review status.
     */
    fun getFullAppStatusJson(context: Context, huaweiAppId: String? = null): String {
        return JSONObject().apply {
            put("app", getAppInfoJson(context))
            put("device", getDeviceInfoJson(context))
            put("network", getNetworkStatusJson(context))
            put("battery", getBatteryStatusJson(context))
            put("permissions", getPermissionsStatusJson(context))
            put("reviews", getReviewStatusJson(context, huaweiAppId))
        }.toString()
    }

    /**
     * Returns application identity, versions, and installation source.
     */
    fun getAppInfoJson(context: Context): JSONObject {
        val pm = context.packageManager
        val packageName = context.packageName
        val pInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
        } catch (e: Exception) {
            null
        }

        val appName = try {
            context.applicationInfo.loadLabel(pm).toString()
        } catch (e: Exception) {
            packageName
        }

        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val installer = getInstallerPackage(context)
        val installerStoreName = when {
            installer == "com.android.vending" -> "Google Play Store"
            installer?.contains("huawei") == true -> "Huawei AppGallery"
            installer == "com.amazon.venezia" -> "Amazon Appstore"
            installer?.contains("samsung") == true -> "Samsung Galaxy Store"
            installer.isNullOrBlank() -> "Sideload / Developer Build"
            else -> installer
        }

        return JSONObject().apply {
            put("packageName", packageName)
            put("appName", appName)
            put("versionName", pInfo?.versionName ?: "1.0.0")
            put("versionCode", pInfo?.let { PackageInfoCompat.getLongVersionCode(it) } ?: 1L)
            put("minSdk", context.applicationInfo.minSdkVersion)
            put("targetSdk", context.applicationInfo.targetSdkVersion)
            put("isDebug", isDebug)
            put("firstInstallTime", pInfo?.firstInstallTime ?: 0L)
            put("lastUpdateTime", pInfo?.lastUpdateTime ?: 0L)
            put("installerPackage", installer ?: "")
            put("installerStoreName", installerStoreName)
        }
    }

    /**
     * Returns hardware, OS version, display metrics, and locale info.
     */
    fun getDeviceInfoJson(context: Context): JSONObject {
        val dm = context.resources.displayMetrics
        val config = context.resources.configuration
        val isTablet = (config.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val isTv = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        val orientation = if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) "LANDSCAPE" else "PORTRAIT"

        return JSONObject().apply {
            put("manufacturer", Build.MANUFACTURER ?: "")
            put("model", Build.MODEL ?: "")
            put("brand", Build.BRAND ?: "")
            put("device", Build.DEVICE ?: "")
            put("hardware", Build.HARDWARE ?: "")
            put("androidVersion", Build.VERSION.RELEASE ?: "")
            put("sdkInt", Build.VERSION.SDK_INT)
            put("language", Locale.getDefault().language)
            put("country", Locale.getDefault().country)
            put("localeTag", Locale.getDefault().toLanguageTag())
            put("timeZone", TimeZone.getDefault().id)
            put("isTablet", isTablet)
            put("isTv", isTv)
            put("screen", JSONObject().apply {
                put("widthPx", dm.widthPixels)
                put("heightPx", dm.heightPixels)
                put("densityDpi", dm.densityDpi)
                put("density", dm.density.toDouble())
                put("orientation", orientation)
            })
        }
    }

    /**
     * Returns live network connection type and status.
     */
    fun getNetworkStatusJson(context: Context): JSONObject {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val capabilities = cm?.getNetworkCapabilities(activeNetwork)

        val isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val type = when {
            capabilities == null -> "NONE"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "BLUETOOTH"
            else -> "OTHER"
        }
        val isMetered = cm?.isActiveNetworkMetered == true

        return JSONObject().apply {
            put("isOnline", isOnline)
            put("type", type)
            put("isMetered", isMetered)
        }
    }

    /**
     * Returns battery level, charging state, and power saver state.
     */
    fun getBatteryStatusJson(context: Context): JSONObject {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val pluggedSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "WIRELESS"
            else -> "BATTERY"
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isPowerSaveMode = powerManager?.isPowerSaveMode == true

        return JSONObject().apply {
            put("level", batteryPct)
            put("isCharging", isCharging)
            put("pluggedSource", pluggedSource)
            put("isPowerSaveMode", isPowerSaveMode)
        }
    }

    /**
     * Returns permission statuses (e.g. notifications).
     */
    fun getPermissionsStatusJson(context: Context): JSONObject {
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        return JSONObject().apply {
            put("notificationsEnabled", notificationsEnabled)
        }
    }

    /**
     * Returns In-App Review capability, store identification, and direct review URLs.
     */
    fun getReviewStatusJson(context: Context, huaweiAppId: String? = null): JSONObject {
        val isHuawei = isHuaweiDevice()
        val packageName = context.packageName

        val store = if (isHuawei) "HUAWEI" else "GOOGLE"
        val storeReviewUrl = if (isHuawei) {
            val id = huaweiAppId ?: packageName
            "https://appgallery.huawei.com/app/C$id"
        } else {
            "https://play.google.com/store/apps/details?id=$packageName"
        }

        val marketUri = if (isHuawei) {
            "appmarket://details?id=$packageName"
        } else {
            "market://details?id=$packageName"
        }

        return JSONObject().apply {
            put("isReviewSupported", true)
            put("activeStore", store)
            put("storeReviewUrl", storeReviewUrl)
            put("marketReviewUri", marketUri)
        }
    }

    private fun getInstallerPackage(context: Context): String? {
        return try {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(context.packageName)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isHuaweiDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        val brand = Build.BRAND?.lowercase() ?: ""
        return manufacturer.contains("huawei") || manufacturer.contains("honor") ||
                brand.contains("huawei") || brand.contains("honor")
    }
}
