package com.arny.data.db.intities

import android.arch.persistence.room.*
import com.arny.data.models.FlightEntity

@Entity(tableName = "times_to_flights",
        indices = [Index("flight","time_type")],
        foreignKeys = [
                ForeignKey(entity = FlightEntity::class, parentColumns = ["_id"], childColumns = ["flight"], onDelete = ForeignKey.CASCADE),
                ForeignKey(entity = TimeTypeEntity::class, parentColumns = ["_id"], childColumns = ["time_type"], onDelete = ForeignKey.NO_ACTION)
        ]
)
data class TimeToFlightEntity(
        @PrimaryKey(autoGenerate = true)
        var _id: Long? = null,
        var flight: Long? = null,
        @ColumnInfo(name = "time_type")
        var timeType: Long? = null,
        @Ignore
        var timeTypeEntity: TimeTypeEntity? = null,
        var time: Int = 0,//minutes
        @ColumnInfo(name = "add_flight_time")
        var addToFlightTime: Boolean = false) {

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as TimeToFlightEntity
                if (flight != other.flight) return false
                if (timeType != other.timeType) return false
                if (timeTypeEntity != other.timeTypeEntity) return false
                if (time != other.time) return false
                if (addToFlightTime != other.addToFlightTime) return false

                return true
        }

        override fun hashCode(): Int {
                var result = flight?.hashCode() ?: 0
                result = 31 * result + (timeType?.hashCode() ?: 0)
                result = 31 * result + (timeTypeEntity?.hashCode() ?: 0)
                result = 31 * result + time.hashCode()
                result = 31 * result + addToFlightTime.hashCode()
                return result
        }
}