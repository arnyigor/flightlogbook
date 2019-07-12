package com.arny.flightlogbook.data.source

import com.arny.flightlogbook.data.db.daos.AircraftTypeDAO
import com.arny.flightlogbook.data.models.PlaneType
import com.arny.flightlogbook.data.source.base.BaseRepository


interface TypesRepository : BaseRepository {
    fun getCraftTypeDAO(): AircraftTypeDAO

    fun loadPlaneTypes(): List<PlaneType> {
        return getCraftTypeDAO().queryAircraftTypes()
    }

    fun loadType(id: Long): PlaneType? {
        return getCraftTypeDAO().queryAircraftType(id)
    }

    fun addType(name: String): Boolean {
        val type = PlaneType()
        type.typeName = name
        return getCraftTypeDAO().insertReplace(type) > 0
    }

    fun removeType(type: PlaneType?): Boolean {
        return getCraftTypeDAO().delete(type?.typeId) > 0
    }

    fun removeTypes(): Boolean {
        return getCraftTypeDAO().delete() > 0
    }

    fun updateType(type: PlaneType?): Boolean {
        return type?.let { getCraftTypeDAO().updateReplace(type) > 0 } ?: false
    }

    fun updatePlaneTypeTitle(type: PlaneType?, title: String?): Boolean {
        return getCraftTypeDAO().setTitle(type?.typeId, title) > 0
    }

    fun getAircraftTypesCount(): Int {
        val cursor = getCraftTypeDAO().queryAirplaneTypesCount()
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            cursor.close()
        }
        return count
    }
}