package com.arny.flightlogbook.domain.models

sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable?) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}

fun <T> T.toResult(): Result<T> = try {
    Result.Success(this)
} catch (e: Exception) {
    Result.Error(e)
}

fun Throwable.toResult() = Result.Error(this)
