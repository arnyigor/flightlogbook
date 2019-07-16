package com.arny.domain.models

import com.arny.data.models.PlaneTypeEntity

class PlaneType(var typeId: Long = 0) {
    var typeName: String? = null

    override fun toString(): String {
        return "PlaneType(typeId=$typeId, typeName=$typeName)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlaneType
        if (typeId != other.typeId) return false
        if (typeName != other.typeName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = typeId.hashCode()
        result = 31 * result + (typeName?.hashCode() ?: 0)
        return result
    }

}

fun PlaneTypeEntity.toPlaneType(): PlaneType {
    val planeType = PlaneType(typeId)
    planeType.typeName = typeName
    return planeType
}

fun PlaneType.toPlaneTypeEntity(): PlaneTypeEntity {
    val planeTypeEntity = PlaneTypeEntity(typeId)
    planeTypeEntity.typeName = typeName
    return planeTypeEntity
}
