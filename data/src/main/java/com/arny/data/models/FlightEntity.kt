package com.arny.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arny.domain.models.Flight
import com.arny.helpers.utils.DateTimeUtils

@Entity(tableName = "main_table")
data class FlightEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long? = null) {
    var date: String? = null
    var datetime: Long? = null

    @ColumnInfo(name = "log_time")
    var logtime: Int? = null

    @ColumnInfo(name = "ground_time")
    var groundTime: Int? = null

    @ColumnInfo(name = "night_time")
    var nightTime: Int? = null

    @ColumnInfo(name = "reg_no")
    var regNo: String? = null

    @ColumnInfo(name = "airplane_type")
    var planeId: Long? = null

    @ColumnInfo(name = "day_night")
    var daynight: Int? = null

    @ColumnInfo(name = "ifr_vfr")
    var ifrvfr: Int? = null

    @ColumnInfo(name = "flight_type")
    var flighttype: Int? = null
    var description: String? = null

    override fun toString(): String {
        return "FlightEntity(id=$id, date=$date, datetime=$datetime, logtime=$logtime, regNo=$regNo, groundTime=$groundTime,  nightTime=$nightTime, planeId=$planeId, daynight=$daynight, ifrvfr=$ifrvfr, flighttype=$flighttype, description=$description)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FlightEntity
        if (id != other.id) return false
        if (date != other.date) return false
        if (datetime != other.datetime) return false
        if (logtime != other.logtime) return false
        if (groundTime != other.groundTime) return false
        if (nightTime != other.nightTime) return false
        if (regNo != other.regNo) return false
        if (planeId != other.planeId) return false
        if (daynight != other.daynight) return false
        if (ifrvfr != other.ifrvfr) return false
        if (flighttype != other.flighttype) return false
        if (description != other.description) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (datetime?.hashCode() ?: 0)
        result = 31 * result + (logtime ?: 0)
        result = 31 * result + (groundTime ?: 0)
        result = 31 * result + (nightTime ?: 0)
        result = 31 * result + (regNo?.hashCode() ?: 0)
        result = 31 * result + (planeId?.hashCode() ?: 0)
        result = 31 * result + (daynight ?: 0)
        result = 31 * result + (ifrvfr ?: 0)
        result = 31 * result + (flighttype ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    fun toFlight(): Flight {
        val flight = Flight(id)
        flight.datetime = this.datetime
        flight.datetimeFormatted = this.datetime?.let { DateTimeUtils.getDateTime(it, "dd MMM yyyy") }
        flight.flightTime = this.logtime ?: 0
        flight.logtimeFormatted = this.logtime?.let { DateTimeUtils.strLogTime(it) }
        flight.regNo = this.regNo
        flight.totalTime = (this.logtime ?: 0) + (this.groundTime ?: 0)
        flight.nightTime = this.nightTime ?: 0
        flight.groundTime = this.groundTime ?: 0
        flight.planeId = this.planeId
        flight.daynight = this.daynight
        flight.ifrvfr = this.ifrvfr
        flight.flightTypeId = this.flighttype
        flight.description = this.description
        return flight
    }

}

fun Flight.toFlightEntity(): FlightEntity {
    val flight = FlightEntity(id)
    flight.datetime = this.datetime
    flight.logtime = this.flightTime
    flight.regNo = this.regNo
    flight.planeId = this.planeId
    flight.daynight = this.daynight
    flight.ifrvfr = this.ifrvfr
    flight.flighttype = this.flightTypeId
    flight.description = this.description
    return flight
}
