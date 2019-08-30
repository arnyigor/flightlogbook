package com.arny.data.repositories

import com.arny.data.db.daos.FlightTypeDAO
import com.arny.data.models.FlightTypeEntity
import com.arny.data.repositories.base.BaseRepository


interface FlightTypesRepository : BaseRepository {
    fun getFlightTypeDAO(): FlightTypeDAO

    fun loadDBFlightTypes(): List<FlightTypeEntity> {
        return getFlightTypeDAO().queryTypes()
    }

    fun loadDBFlightType(id: Long?): FlightTypeEntity? {
        return getFlightTypeDAO().queryFlightType(id)
    }

    fun addFlightType(name: String): Boolean {
        val type = FlightTypeEntity()
        type.typeTitle = name
        return getFlightTypeDAO().insertReplace(type) > 0
    }

    fun addFlightTypeAndGet(name: String): Long {
        val type = FlightTypeEntity()
        type.typeTitle = name
        return getFlightTypeDAO().insertReplace(type)
    }

    fun addFlightType(type: FlightTypeEntity): Boolean {
        return getFlightTypeDAO().insertReplace(type) > 0
    }

    fun addFlightTypes(types: List<FlightTypeEntity>): Boolean {
        val ids = getFlightTypeDAO().insertReplace(types)
        return !ids.contains(0)
    }

    fun removeFlightType(type: FlightTypeEntity?): Boolean {
        return getFlightTypeDAO().delete(type?.id) > 0
    }

    fun removeFlightType(id: Long?): Boolean {
        return getFlightTypeDAO().delete(id) > 0
    }

    fun removeFlightTypes(): Boolean {
        return getFlightTypeDAO().delete() > 0
    }

    fun updateFlightTypeTitle(id: Long?, title: String?): Boolean {
        return getFlightTypeDAO().setTitle(id, title) > 0
    }

}