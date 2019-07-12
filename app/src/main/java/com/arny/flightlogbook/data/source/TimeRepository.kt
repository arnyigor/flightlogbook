package com.arny.flightlogbook.data.source

import com.arny.flightlogbook.data.db.daos.TimeTypeDAO
import com.arny.flightlogbook.data.db.intities.TimeTypeEntity

/**
 *Created by Sedoy on 12.07.2019
 */
interface TimeRepository {
    fun getTimeTipeDAO(): TimeTypeDAO

    fun queryDBFlightTimes(flightId: Long?): List<TimeTypeEntity> {
        return getTimeTipeDAO().queryTimeTypes(flightId)
    }

    fun addDBFlightTime(flightId: Long?, title: String?, time: Long): Boolean {
        val entity = TimeTypeEntity(null, flightId, time)
        return getTimeTipeDAO().insertReplace(entity) > 0
    }

    fun removeDBFlightTime(_id: Long?): Boolean {
        return getTimeTipeDAO().delete(_id) > 0
    }

    fun updateDBFlightTime(time: TimeTypeEntity): Boolean {
        return getTimeTipeDAO().updateReplace(time) > 0
    }

    fun updateDBFlightTimes(times: Collection<TimeTypeEntity>): Boolean {
        return getTimeTipeDAO().updateReplace(times) > 0
    }
}