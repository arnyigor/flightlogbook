package com.arny.domain.models


data class TimeToFlight(
        var _id: Long? = null,
        var flight: Long? = null,
        var timeType: Long? = null,
        var timeTypeEntity: TimeType? = null,
        var time: Long = 0,//minutes
        var addToFlightTime: Boolean = false) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TimeToFlight
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