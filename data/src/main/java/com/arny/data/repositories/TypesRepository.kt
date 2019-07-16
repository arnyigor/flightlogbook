package com.arny.data.repositories

import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.models.PlaneTypeEntity
import com.arny.data.repositories.base.BaseRepository


interface TypesRepository : BaseRepository {
    fun getCraftTypeDAO(): AircraftTypeDAO

    fun loadPlaneTypes(): List<PlaneTypeEntity> {
        return getCraftTypeDAO().queryAircraftTypes()
    }

    fun loadType(id: Long): PlaneTypeEntity? {
        return getCraftTypeDAO().queryAircraftType(id)
    }

    fun addType(name: String): Boolean {
        val type = PlaneTypeEntity()
        type.typeName = name
        return getCraftTypeDAO().insertReplace(type) > 0
    }

    fun removeType(type: PlaneTypeEntity?): Boolean {
        return getCraftTypeDAO().delete(type?.typeId) > 0
    }

    fun removeTypes(): Boolean {
        return getCraftTypeDAO().delete() > 0
    }

    fun updateType(type: PlaneTypeEntity?): Boolean {
        return type?.let { getCraftTypeDAO().updateReplace(type) > 0 } ?: false
    }

    fun updatePlaneTypeTitle(type: PlaneTypeEntity?, title: String?): Boolean {
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