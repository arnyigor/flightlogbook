package com.arny.domain.models

import com.arny.data.models.FlightEntity
import com.arny.helpers.utils.DateTimeUtils

class Flight(var id: Long? = null) {
    var datetime: Long? = null
    var flightTime: Int  = 0
    var nightTime: Int = 0
    var ifrTime: Int  = 0
    var groundTime: Int  = 0
    var workTime: Int = 0
    var totalTime: Int = 0
    var regNo: String? = null
    var planeId: Long? = null
    var planeType: PlaneType? = null
    var daynight: Int? = null
    var ifrvfr: Int? = null
    var flightTypeId: Int? = null
    var flightType: FlightType? = null
    var description: String? = null
    var datetimeFormatted: String? = null
    var logtimeFormatted: String? = null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Flight
        if (id != other.id) return false
        if (datetime != other.datetime) return false
        if (flightTime != other.flightTime) return false
        if (nightTime != other.nightTime) return false
        if (ifrTime != other.ifrTime) return false
        if (groundTime != other.groundTime) return false
        if (workTime != other.workTime) return false
        if (regNo != other.regNo) return false
        if (planeId != other.planeId) return false
        if (daynight != other.daynight) return false
        if (ifrvfr != other.ifrvfr) return false
        if (flightTypeId != other.flightTypeId) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (datetime?.hashCode() ?: 0)
        result = 31 * result + flightTime
        result = 31 * result + nightTime
        result = 31 * result + groundTime
        result = 31 * result + ifrTime
        result = 31 * result + workTime
        result = 31 * result + (regNo?.hashCode() ?: 0)
        result = 31 * result + (planeId?.hashCode() ?: 0)
        result = 31 * result + (daynight ?: 0)
        result = 31 * result + (ifrvfr ?: 0)
        result = 31 * result + (flightTypeId ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Flight(id=$id, datetime=$datetime, flightTime=$flightTime,  nightTime=$nightTime,  groundTime=$groundTime, sumFlightTime=$totalTime, regNo=$regNo, ifrTime=$ifrTime, planeId=$planeId, daynight=$daynight, ifrvfr=$ifrvfr, flightTypeId=$flightTypeId, description=$description)"
    }
}

fun FlightEntity.toFlight(): Flight {
    val flight = Flight(id)
    flight.datetime = this.datetime
    flight.datetimeFormatted = this.datetime?.let { DateTimeUtils.getDateTime(it, "dd MMM yyyy") }
    flight.flightTime = this.logtime?:0
    flight.logtimeFormatted = this.logtime?.let { DateTimeUtils.strLogTime(it) }
    flight.regNo = this.regNo
    flight.planeId = this.planeId
    flight.daynight = this.daynight
    flight.ifrvfr = this.ifrvfr
    flight.flightTypeId = this.flighttype
    flight.description = this.description
    return flight
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
