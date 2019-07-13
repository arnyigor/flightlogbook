package com.arny.flightlogbook.data.source

import com.arny.flightlogbook.data.db.daos.TimeToFlightsDAO
import com.arny.flightlogbook.data.db.daos.TimeTypesDAO
import com.arny.flightlogbook.data.db.intities.TimeToFlightEntity
import com.arny.flightlogbook.data.db.intities.TimeTypeEntity

/**
 *Created by Sedoy on 12.07.2019
 */
interface TimesRepository {
    fun getTimeTypeDAO(): TimeTypesDAO
    fun getTimeToFlightsDAO(): TimeToFlightsDAO

    fun addDBTimeType(title: String?): Boolean {
        return getTimeTypeDAO().insertReplace(TimeTypeEntity(null, title)) > 0
    }

    fun removeDBFlightTime(_id: Long?): Boolean {
        return getTimeTypeDAO().delete(_id) > 0
    }

    fun updateDBFlightTime(typeId: Long?, title: String?): Boolean {
        return getTimeTypeDAO().setTitle(typeId, title) > 0
    }

    fun queryDBFlightsTimes(): List<TimeToFlightEntity> {
        return getTimeToFlightsDAO().queryTimesToFlights()
    }

    fun queryDBFlightTimes(flightId: Long?): List<TimeToFlightEntity> {
        return getTimeToFlightsDAO().queryTimesOfFlight(flightId)
    }

    fun addDBFlightTime(flightId: Long?, timeType: Long?, time: Long, addToFlight: Boolean = false): Boolean {
        val entity = TimeToFlightEntity(null, flightId, timeType, null, time, addToFlight)
        return getTimeToFlightsDAO().insertReplace(entity) > 0
    }

    fun updateDBFlightTime(flightId: Long?, timeType: Long?, time: Long, addToFlight: Boolean = false): Boolean {
        return getTimeToFlightsDAO().setFlightTime(flightId, timeType, time, addToFlight) > 0
    }

    fun removeDBFlightTime(flightId: Long?, timeType: Long?): Boolean {
        return getTimeToFlightsDAO().deleteTimeFromFlight(flightId, timeType) > 0
    }
}