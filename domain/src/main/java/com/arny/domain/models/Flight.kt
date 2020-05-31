package com.arny.domain.models

class Flight(var id: Long? = null) {
    var datetime: Long? = null
    var flightTime: Int = 0
    var nightTime: Int = 0
    var ifrTime: Int = 0
    var groundTime: Int = 0
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
    var params: Params? = null
    var colorInt: Int? = null
    var colorText: Int? = null

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
        if (totalTime != other.totalTime) return false
        if (regNo != other.regNo) return false
        if (planeId != other.planeId) return false
        if (daynight != other.daynight) return false
        if (ifrvfr != other.ifrvfr) return false
        if (flightTypeId != other.flightTypeId) return false
        if (description != other.description) return false
        if (colorInt != other.colorInt) return false
        if (colorText != other.colorText) return false
        if (params != params) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (datetime?.hashCode() ?: 0)
        result = 31 * result + flightTime
        result = 31 * result + nightTime
        result = 31 * result + groundTime
        result = 31 * result + ifrTime
        result = 31 * result + totalTime
        result = 31 * result + (regNo?.hashCode() ?: 0)
        result = 31 * result + (planeId?.hashCode() ?: 0)
        result = 31 * result + (daynight ?: 0)
        result = 31 * result + (ifrvfr ?: 0)
        result = 31 * result + (flightTypeId ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (colorInt?.hashCode() ?: 0)
        result = 31 * result + (colorText?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Flight(id=$id, " +
                "datetime=$datetime, " +
                "flightTime=$flightTime, " +
                "nightTime=$nightTime,  " +
                "groundTime=$groundTime, " +
                "sumFlightTime=$totalTime, " +
                "regNo=$regNo, " +
                "ifrTime=$ifrTime, " +
                "planeId=$planeId, " +
                "daynight=$daynight, " +
                "ifrvfr=$ifrvfr, " +
                "flightTypeId=$flightTypeId, " +
                "colorInt=$colorInt, " +
                "colorText=$colorText, " +
                "description=$description)"
    }
}
