package com.arny.domain.models

data class BusinessException constructor(override val message: String? = null, override val cause: Throwable? = null) : Throwable(message, cause) {
    constructor(cause: Throwable?) : this(cause?.message, cause)
}
