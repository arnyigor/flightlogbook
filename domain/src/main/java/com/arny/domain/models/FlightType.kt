package com.arny.domain.models

import com.arny.helpers.utils.Utility

data class FlightType ( var id: Long? = null) {
    var typeTitle: String? = null

    override fun toString(): String {
        return Utility.getFields(this)
    }
}
