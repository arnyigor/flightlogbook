package com.arny.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "flight_type")
data class FlightTypeEntity (@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long? = null) {
    @ColumnInfo(name = "title")
    var typeTitle: String? = null

    override fun toString(): String {
        return "FlightTypeEntity(id=$id, typeTitle=$typeTitle)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlightTypeEntity
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
