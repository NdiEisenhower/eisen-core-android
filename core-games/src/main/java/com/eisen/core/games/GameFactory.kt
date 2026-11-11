package com.eisen.core.games

import android.content.Context
import android.os.Build

object GameFactory {

    fun create(
        context: Context,
        store: GameStore = GameStore.AUTO
    ): IGameManager {
        return when (store) {
            GameStore.GOOGLE -> GoogleGameManager()
            GameStore.HUAWEI -> HuaweiGameManager()
            GameStore.AUTO -> {
                if (isHuaweiDevice(context)) {
                    HuaweiGameManager()
                } else {
                    GoogleGameManager()
                }
            }
        }
    }

    private fun isHuaweiDevice(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        val brand = Build.BRAND?.lowercase() ?: ""
        return manufacturer.contains("huawei") || manufacturer.contains("honor") ||
                brand.contains("huawei") || brand.contains("honor")
    }
}

