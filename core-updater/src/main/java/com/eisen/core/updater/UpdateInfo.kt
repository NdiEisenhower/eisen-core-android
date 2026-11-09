package com.eisen.core.updater

enum class UpdateType {
    NONE,
    SOFT,
    HARD
}

data class UpdateInfo(
    val type: UpdateType = UpdateType.NONE,
    val latestVersionCode: Int = 0,
    val minRequiredVersionCode: Int = 0,
    val storeUrl: String? = null
) {
    val isHardUpdate: Boolean get() = type == UpdateType.HARD
    val isSoftUpdate: Boolean get() = type == UpdateType.SOFT
    val isUpdateRequired: Boolean get() = type != UpdateType.NONE
}

