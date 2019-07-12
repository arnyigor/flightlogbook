package com.arny.flightlogbook.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "type_table")
data class PlaneType(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "type_id") var typeId: Long = 0) {
    @ColumnInfo(name = "airplane_type")
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
