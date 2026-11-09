package com.eisen.core.common.model

sealed class CoreResult<out T> {
    data class Success<out T>(val data: T) : CoreResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : CoreResult<Nothing>()
    object Loading : CoreResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    fun getOrNull(): T? = (this as? Success)?.data
}

