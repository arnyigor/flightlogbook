package com.arny.domain.models

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

/**
 * `true` if [Result] is of type [Success] & holds non-null [Success.data].
 */
val Result<*>.succeeded
    get() = this is Result.Success && data != null

fun <T> T.toResult(): Result<T> {
    return try {
        Result.Success(this)
    } catch (e: Exception) {
        Result.Error(e)
    }
}

fun Throwable.toResult() = Result.Error(this)
