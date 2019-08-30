package com.arny.data.repositories

import com.arny.data.db.daos.TimeToFlightsDAO
import com.arny.data.db.daos.TimeTypesDAO
import com.arny.data.db.intities.TimeToFlightEntity
import com.arny.data.db.intities.TimeTypeEntity

/**
 *Created by Sedoy on 12.07.2019
 */
interface TimesRepository {
    fun getTimeTypeDAO(): TimeTypesDAO
    fun getTimeToFlightsDAO(): TimeToFlightsDAO

    fun queryDBTimeTypes(): List<TimeTypeEntity> {
        return getTimeTypeDAO().queryTimeTypes()
    }
    fun queryDBTimeType(typeId:Long): TimeTypeEntity? {
        return getTimeTypeDAO().queryTimeType(typeId)
    }

    fun addDBTimeType(title: String?): Boolean {
        return getTimeTypeDAO().insertReplace(TimeTypeEntity(null, title)) > 0
    }

    fun addDBTimeTypeAndGet(title: String?): Long {
        return getTimeTypeDAO().insertReplace(TimeTypeEntity(null, title))
    }

    fun updateDBTimeType(typeId: Long?, title: String?): Boolean {
        return getTimeTypeDAO().setTitle(typeId, title) > 0
    }

    fun removeDBTimeType(_id: Long?): Boolean {
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

    fun deleteDBFlightTimesByFlight(flightId: Long?): Boolean {
        return getTimeToFlightsDAO().deleteTimesFromFlight(flightId)>0
    }

    fun deleteDBFlightTimesByTime(timeId: Long?): Boolean {
        return getTimeToFlightsDAO().delete(timeId)>0
    }

    fun queryDBFlightTimesSum(flightId: Long?,addToFlight: Boolean): Int {
        val cursor = getTimeToFlightsDAO().queryFlightTimesSum(flightId, addToFlight)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            cursor.close()
        }
        return count
    }

    fun queryDBFlightsTimesSum(addToFlight: Boolean? = null): Int {
        val cursor = if (addToFlight != null) getTimeToFlightsDAO().queryFlightTimesSum(addToFlight) else getTimeToFlightsDAO().queryFlightTimesSum()
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            cursor.close()
        }
        return count
    }

    fun insertDBFlightTime(flightId: Long?, timeType: Long?, time: Int, addToFlight: Boolean = false): Boolean {
        val entity = TimeToFlightEntity(null, flightId, timeType, null, time, addToFlight)
        return getTimeToFlightsDAO().insertReplace(entity) > 0
    }

    fun insertDBFlightTime(entity:TimeToFlightEntity): Boolean {
        return getTimeToFlightsDAO().insertReplace(entity) > 0
    }

    fun insertDBFlightTimes(entities:List<TimeToFlightEntity>): Boolean {
        return !getTimeToFlightsDAO().insertReplace(entities).contains(0)
    }

    fun updateDBFlightTime(flightId: Long?, timeType: Long?, time: Int, addToFlight: Boolean = false): Boolean {
        return getTimeToFlightsDAO().setFlightTime(flightId, timeType, time, addToFlight) > 0
    }

    fun removeDBFlightTime(flightId: Long?, timeType: Long?): Boolean {
        return getTimeToFlightsDAO().deleteTimeFromFlight(flightId, timeType) > 0
    }
}