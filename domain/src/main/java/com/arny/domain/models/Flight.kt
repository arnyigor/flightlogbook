package com.arny.domain.models

import com.arny.data.models.FlightEntity
import com.arny.helpers.utils.DateTimeUtils

class Flight(var id: Long? = null) {
    var date: String? = null
    var datetime: Long? = null
    var datetimeFormatted: String? = null
    var logtime: Int? = null
    var sumlogTime: Int? = null
    var sumFlightTime: Int? = null
    var sumGroundTime: Int? = null
    var logtimeFormatted: String? = null
    var reg_no: String? = null
    var airplanetypetitle: String? = null
    var planeId: Long? = null
    var daynight: Int? = null
    var ifrvfr: Int? = null
    var flightTypeId: Int? = null
    var flightType: FlightType? = null
    var description: String? = null
    var times: List<TimeToFlight>?=null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Flight
        if (id != other.id) return false
        if (date != other.date) return false
        if (datetime != other.datetime) return false
        if (logtime != other.logtime) return false
        if (sumlogTime != other.sumlogTime) return false
        if (sumFlightTime != other.sumFlightTime) return false
        if (sumGroundTime != other.sumGroundTime) return false
        if (reg_no != other.reg_no) return false
        if (airplanetypetitle != other.airplanetypetitle) return false
        if (planeId != other.planeId) return false
        if (daynight != other.daynight) return false
        if (ifrvfr != other.ifrvfr) return false
        if (flightTypeId != other.flightTypeId) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (datetime?.hashCode() ?: 0)
        result = 31 * result + (logtime ?: 0)
        result = 31 * result + (sumlogTime ?: 0)
        result = 31 * result + (sumFlightTime ?: 0)
        result = 31 * result + (sumGroundTime ?: 0)
        result = 31 * result + (reg_no?.hashCode() ?: 0)
        result = 31 * result + (airplanetypetitle?.hashCode() ?: 0)
        result = 31 * result + (planeId?.hashCode() ?: 0)
        result = 31 * result + (daynight ?: 0)
        result = 31 * result + (ifrvfr ?: 0)
        result = 31 * result + (flightTypeId ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Flight(id=$id, date=$date, datetime=$datetime, logtime=$logtime, sumlogTime=$sumlogTime,  sumFlightTime=$sumFlightTime,  sumGroundTime=$sumGroundTime, reg_no=$reg_no, airplanetypetitle=$airplanetypetitle, planeId=$planeId, daynight=$daynight, ifrvfr=$ifrvfr, flightTypeId=$flightTypeId, description=$description)"
    }
}

fun FlightEntity.toFlight(): Flight {
    val flight = Flight(id)
    flight.date = this.date
    flight.datetime = this.datetime
    flight.datetimeFormatted = this.datetime?.let { DateTimeUtils.getDateTime(it, "dd MMM yyyy") }
    flight.logtime = this.logtime
    flight.logtimeFormatted = this.logtime?.let { DateTimeUtils.strLogTime(it) }
    flight.reg_no = this.reg_no
    flight.airplanetypetitle = this.airplanetypetitle
    flight.planeId = this.aircraft_id
    flight.daynight = this.daynight
    flight.ifrvfr = this.ifrvfr
    flight.flightTypeId = this.flighttype
    flight.description = this.description
    return flight
}


fun Flight.toFlightEntity(): FlightEntity {
    val flight = FlightEntity(id)
    flight.date = this.date
    flight.datetime = this.datetime
    flight.logtime = this.logtime
    flight.reg_no = this.reg_no
    flight.airplanetypetitle = this.airplanetypetitle
    flight.aircraft_id = this.planeId
    flight.daynight = this.daynight
    flight.ifrvfr = this.ifrvfr
    flight.flighttype = this.flightTypeId
    flight.description = this.description
    return flight
}
