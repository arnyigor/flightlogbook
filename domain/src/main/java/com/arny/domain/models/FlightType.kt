package com.arny.domain.models


data class FlightType ( var id: Long? = null) {
    var typeTitle: String? = null
    override fun toString(): String {
        return "FlightType(id=$id, typeTitle=$typeTitle)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlightType
        if (id != other.id) return false
        if (typeTitle != other.typeTitle) return false
        return true
    }
    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (typeTitle?.hashCode() ?: 0)
        return result
    }
}


