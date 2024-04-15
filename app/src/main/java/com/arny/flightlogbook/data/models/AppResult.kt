package com.arny.flightlogbook.data.models

sealed class AppResult<out R> {

    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val exception: Throwable?) : AppResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}

fun <T> T.toResult(): AppResult<T> = try {
    AppResult.Success(this)
} catch (e: Exception) {
    AppResult.Error(e)
}

fun Throwable.toResult() = AppResult.Error(this)
