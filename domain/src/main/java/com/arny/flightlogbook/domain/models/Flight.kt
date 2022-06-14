package com.arny.flightlogbook.domain.models

import com.arny.flightlogbook.customfields.models.CustomFieldValue

data class Flight(
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
    var flightTypeId: Long? = null,
    var flightType: FlightType? = null,
    var description: String? = null,
    var datetimeFormatted: String? = null,
    var flightTimeFormatted: String? = null,
    var customParams: MutableMap<String, Any?>? = null,
    var colorInt: Int? = null,
    var colorText: Int? = null,
    var selected: Boolean = false,
    var departureId: Long? = null,
    var arrivalId: Long? = null,
    var departureUtcTime: Int? = null,
    var arrivalUtcTime: Int? = null,
    var departure: Airport? = null,
    var arrival: Airport? = null,
    var customFieldsValues: List<CustomFieldValue>? = null,
)
