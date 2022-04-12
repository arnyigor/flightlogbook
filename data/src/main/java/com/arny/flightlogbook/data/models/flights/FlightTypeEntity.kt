package com.arny.flightlogbook.data.models.flights

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arny.flightlogbook.domain.models.FlightType

@Entity(tableName = "flight_type")
data class FlightTypeEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id") var id: Long? = null,
        @ColumnInfo(name = "title")
        var typeTitle: String? = null
) {

    fun toFlightType(): FlightType = FlightType(this.id, this.typeTitle)
}

fun FlightType.toFlightTypeEntity(): FlightTypeEntity = FlightTypeEntity(id, typeTitle)
