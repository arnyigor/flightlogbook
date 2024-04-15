package com.arny.flightlogbook.data.models

enum class FilterType(val index: Int) {
    AIRCRAFT_NAME(0),
    AIRCRAFT_REG_NO(1),
    AIRCRAFT_TYPE(2),
    FLIGHT_TYPE(3),
    COLOR(4)
}