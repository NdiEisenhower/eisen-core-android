package com.eisen.core.games.model

data class GamePlayer(
    val playerId: String,
    val displayName: String,
    val avatarUri: String? = null,
    val provider: String = "UNKNOWN"
)

