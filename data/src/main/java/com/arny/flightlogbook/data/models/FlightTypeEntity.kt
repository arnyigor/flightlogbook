package com.arny.flightlogbook.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arny.domain.models.FlightType

@Entity(tableName = "flight_type")
data class FlightTypeEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long? = null) {
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

    fun toFlightType(): FlightType {
        val type = FlightType(this.id)
        type.typeTitle = this.typeTitle
        return type
    }
}

fun FlightType.toFlightTypeEntity(): FlightTypeEntity {
    val type = FlightTypeEntity(this.id)
    type.typeTitle = this.typeTitle
    return type
}
