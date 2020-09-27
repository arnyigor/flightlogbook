package com.arny.domain.models

data class Flight constructor(
        var id: Long? = null,
        var datetime: Long? = null,
        var flightTime: Int = 0,
        var nightTime: Int = 0,
        var ifrTime: Int = 0,
        var groundTime: Int = 0,
        var totalTime: Int = 0,
        var regNo: String? = null,
        var planeId: Long? = null,
        var planeType: PlaneType? = null,
        var daynight: Int? = null,
        var ifrvfr: Int? = null,
        var flightTypeId: Int? = null,
        var flightType: FlightType? = null,
        var description: String? = null,
        var datetimeFormatted: String? = null,
        var logtimeFormatted: String? = null,
        var params: Params? = null,
        var colorInt: Int? = null,
        var colorText: Int? = null,
        var selected: Boolean = false
)
