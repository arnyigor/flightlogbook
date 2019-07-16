package com.arny.domain.models

import com.arny.data.models.FlightEntity
import com.arny.helpers.utils.DateTimeUtils

class Flight(var id: Long? = null) {
    var date: String? = null
    var datetime: Long? = null
    var datetimeFormatted: String? = null
    var logtime: Int? = null
    var logtimeFormatted: String? = null
    var reg_no: String? = null
    var airplanetypetitle: String? = null
    var aircraft_id: Long? = null
    var daynight: Int? = null
    var ifrvfr: Int? = null
    var flighttype: Int? = null
    var description: String? = null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Flight

        if (id != other.id) return false
        if (date != other.date) return false
        if (datetime != other.datetime) return false
        if (logtime != other.logtime) return false
        if (reg_no != other.reg_no) return false
        if (airplanetypetitle != other.airplanetypetitle) return false
        if (aircraft_id != other.aircraft_id) return false
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
        result = 31 * result + (reg_no?.hashCode() ?: 0)
        result = 31 * result + (airplanetypetitle?.hashCode() ?: 0)
        result = 31 * result + (aircraft_id?.hashCode() ?: 0)
        result = 31 * result + (daynight ?: 0)
        result = 31 * result + (ifrvfr ?: 0)
        result = 31 * result + (flighttype ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Flight(id=$id, date=$date, datetime=$datetime, logtime=$logtime, reg_no=$reg_no, airplanetypetitle=$airplanetypetitle, aircraft_id=$aircraft_id, daynight=$daynight, ifrvfr=$ifrvfr, flighttype=$flighttype, description=$description)"
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
    flight.aircraft_id = this.aircraft_id
    flight.daynight = this.daynight
    flight.ifrvfr = this.ifrvfr
    flight.flighttype = this.flighttype
    flight.description = this.description
    return flight
}


fun Flight.toFlightEntiry(): FlightEntity {
    val flight = FlightEntity(id)
    flight.date = this.date
    flight.datetime = this.datetime
    flight.logtime = this.logtime
    flight.reg_no = this.reg_no
    flight.airplanetypetitle = this.airplanetypetitle
    flight.aircraft_id = this.aircraft_id
    flight.daynight = this.daynight
    flight.ifrvfr = this.ifrvfr
    flight.flighttype = this.flighttype
    flight.description = this.description
    return flight
}
