package com.arny.flightlogbook.data.repositories

import com.arny.domain.models.PlaneType
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.flightlogbook.data.db.daos.AircraftTypeDAO
import com.arny.flightlogbook.data.models.toPlaneTypeEntity
import com.arny.helpers.utils.toItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaneTypesRepositoryImpl @Inject constructor(private val aircraftTypeDAO: AircraftTypeDAO) : PlaneTypesRepository {
    override fun loadPlaneTypes(): List<PlaneType> {
        return aircraftTypeDAO.queryAircraftTypes()
                .map { it.toPlaneType() }
    }

    override fun loadPlaneType(id: Long?): PlaneType? {
        return aircraftTypeDAO.queryAircraftType(id)?.toPlaneType()
    }

    override fun loadPlaneType(title: String?): PlaneType? {
        return aircraftTypeDAO.queryAircraftType(title)?.toPlaneType()
    }

    override fun addType(name: String): Boolean {
        val type = PlaneType()
        type.typeName = name
        val typeEntity = type.toPlaneTypeEntity()
        return aircraftTypeDAO.insertReplace(typeEntity) > 0
    }

    override fun addTypeAndGet(name: String): Long {
        val type = PlaneType()
        type.typeName = name
        val typeEntity = type.toPlaneTypeEntity()
        return aircraftTypeDAO.insertReplace(typeEntity)
    }

    override fun removeType(type: PlaneType?): Boolean {
        return aircraftTypeDAO.delete(type?.typeId) > 0
    }

    override fun removeTypes(): Boolean {
        return aircraftTypeDAO.delete() > 0
    }

    override fun updateType(type: PlaneType?): Boolean {
        return type?.let {
            val typeEntity = type.toPlaneTypeEntity()
            aircraftTypeDAO.updateReplace(typeEntity) > 0
        } ?: false
    }

    override fun updatePlaneTypeTitle(id: Long?, title: String?): Boolean {
        return aircraftTypeDAO.setTitle(id, title) > 0
    }

    override fun getAircraftTypesCount(): Int {
        val cursor = aircraftTypeDAO.queryAirplaneTypesCount()
        var count = 0
        cursor.toItem {
            count = cursor.getInt(0)
        }
        return count
    }

}