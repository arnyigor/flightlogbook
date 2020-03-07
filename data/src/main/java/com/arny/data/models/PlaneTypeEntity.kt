package com.arny.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arny.domain.models.PlaneType

@Entity(tableName = "type_table")
data class PlaneTypeEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "type_id") var typeId: Long? = null) {
    @ColumnInfo(name = "airplane_type")
    var typeName: String? = null

    override fun toString(): String {
        return "PlaneType(typeId=$typeId, typeName=$typeName)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlaneTypeEntity
        if (typeId != other.typeId) return false
        if (typeName != other.typeName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = typeId.hashCode()
        result = 31 * result + (typeName?.hashCode() ?: 0)
        return result
    }

    fun toPlaneType(): PlaneType {
        val planeType = PlaneType(typeId)
        planeType.typeName = typeName
        return planeType
    }
}

fun PlaneType.toPlaneTypeEntity(): PlaneTypeEntity {
    val planeTypeEntity = PlaneTypeEntity(typeId)
    planeTypeEntity.typeName = typeName
    return planeTypeEntity
}
