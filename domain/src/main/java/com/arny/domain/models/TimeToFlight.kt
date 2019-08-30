package com.arny.domain.models

import com.arny.data.db.intities.TimeToFlightEntity


data class TimeToFlight(
        var _id: Long? = null,
        var flight: Long? = null,
        var timeTypeId: Long? = null,
        var timeType: TimeType? = null,
        var time: Int = 0,//minutes
        var addToFlightTime: Boolean = false) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TimeToFlight
        if (flight != other.flight) return false
        if (timeTypeId != other.timeTypeId) return false
        if (time != other.time) return false
        if (addToFlightTime != other.addToFlightTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flight?.hashCode() ?: 0
        result = 31 * result + (timeTypeId?.hashCode() ?: 0)
        result = 31 * result + time.hashCode()
        result = 31 * result + addToFlightTime.hashCode()
        return result
    }
}

fun TimeToFlight?.toTimeEntity(): TimeToFlightEntity {
    val flightEntity = TimeToFlightEntity()
    flightEntity.flight = this?.flight
    flightEntity.time = this?.time?: 0
    flightEntity.timeType = this?.timeTypeId
    flightEntity.timeTypeEntity = this?.timeType.toTimeTypeEntity()
    flightEntity.addToFlightTime = this?.addToFlightTime == true
    return flightEntity
}

fun TimeToFlightEntity?.toTimeFlight(): TimeToFlight {
    return TimeToFlight(this?._id, this?.flight, this?.timeType, this?.timeTypeEntity.toTimeType(), this?.time
            ?: 0, this?.addToFlightTime == true)
}