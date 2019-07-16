package com.arny.domain.models

data class FlightTypeValue(var id: Long? = null) {
    var type: Long? = null
    var typeValue: String? = null
    var typeTitle: String? = null
    override fun toString(): String {
        return "FlightTypeValue(id=$id, type=$type, typeValue=$typeValue, typeTitle=$typeTitle)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FlightTypeValue

        if (id != other.id) return false
        if (type != other.type) return false
        if (typeValue != other.typeValue) return false
        if (typeTitle != other.typeTitle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (typeValue?.hashCode() ?: 0)
        result = 31 * result + (typeTitle?.hashCode() ?: 0)
        return result
    }


}
