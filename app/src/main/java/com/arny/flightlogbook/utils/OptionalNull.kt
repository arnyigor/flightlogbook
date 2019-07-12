package com.arny.flightlogbook.utils

/**
 * For nullable results Rxjava2
 * @param [value] Object maybe==null
 */
class OptionalNull<T>(val value: T?)

fun <T> T?.toOptionalNull(): OptionalNull<T?> {
    return OptionalNull(this)
}
