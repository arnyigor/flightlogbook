package com.arny.domain.models

import com.arny.data.models.FlightTypeEntity

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

fun FlightTypeEntity.toFlightType(): FlightType {
    val type = FlightType(this.id)
    type.typeTitle = this.typeTitle
    return type
}

fun FlightType.toFlightTypeEntity(): FlightTypeEntity {
    val type = FlightTypeEntity(this.id)
    type.typeTitle = this.typeTitle
    return type
}
