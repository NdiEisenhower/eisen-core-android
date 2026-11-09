package com.eisen.core.notifications.model

data class PushTokenResult(
    val token: String?,
    val provider: String,
    val isSuccess: Boolean = !token.isNullOrEmpty(),
    val errorMessage: String? = null
)

