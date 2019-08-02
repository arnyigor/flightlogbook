package com.arny.data.repositories

import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.models.PlaneTypeEntity
import com.arny.data.repositories.base.BaseRepository


interface PlaneTypesRepository : BaseRepository {
    fun getPlaneTypeDAO(): AircraftTypeDAO

    fun loadPlaneTypes(): List<PlaneTypeEntity> {
        return getPlaneTypeDAO().queryAircraftTypes()
    }

    fun loadPlaneType(id: Long?): PlaneTypeEntity? {
        return getPlaneTypeDAO().queryAircraftType(id)
    }

    fun addType(name: String): Boolean {
        val type = PlaneTypeEntity()
        type.typeName = name
        return getPlaneTypeDAO().insertReplace(type) > 0
    }

    fun removeType(type: PlaneTypeEntity?): Boolean {
        return getPlaneTypeDAO().delete(type?.typeId) > 0
    }

    fun removeTypes(): Boolean {
        return getPlaneTypeDAO().delete() > 0
    }

    fun updateType(type: PlaneTypeEntity?): Boolean {
        return type?.let { getPlaneTypeDAO().updateReplace(type) > 0 } ?: false
    }

    fun updatePlaneTypeTitle(id: Long?, title: String?): Boolean {
        return getPlaneTypeDAO().setTitle(id, title) > 0
    }

    fun getAircraftTypesCount(): Int {
        val cursor = getPlaneTypeDAO().queryAirplaneTypesCount()
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            cursor.close()
        }
        return count
    }
}