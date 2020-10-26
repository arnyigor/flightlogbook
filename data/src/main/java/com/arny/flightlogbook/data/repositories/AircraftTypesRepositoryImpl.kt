package com.arny.flightlogbook.data.repositories

import com.arny.core.utils.toItem
import com.arny.domain.models.PlaneType
import com.arny.domain.planetypes.AircraftType
import com.arny.domain.planetypes.AircraftTypesRepository
import com.arny.flightlogbook.data.db.daos.AircraftTypeDAO
import com.arny.flightlogbook.data.models.planes.PlaneTypeEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AircraftTypesRepositoryImpl @Inject constructor(private val aircraftTypeDAO: AircraftTypeDAO) : AircraftTypesRepository {
    override fun loadAircraftTypes(): List<PlaneType> {
        return aircraftTypeDAO.queryAircraftTypes()
                .map { it.toPlaneType() }
    }

    override fun loadAircraftType(id: Long?): PlaneType? {
        return aircraftTypeDAO.queryAircraftType(id)?.toPlaneType()
    }

    override fun loadPlaneTypeByRegNo(regNo: String?): PlaneType? {
        return aircraftTypeDAO.queryAircraftByRegNo(regNo)?.toPlaneType()
    }

    private fun PlaneType.toPlaneTypeEntity(): PlaneTypeEntity {
        if (this.mainType == null) {
            this.mainType = AircraftType.AIRPLANE
        }
        return PlaneTypeEntity(typeId, typeName, regNo, getDBMainType(mainType))
    }

    private fun PlaneTypeEntity.toPlaneType() =
            PlaneType(typeId, typeName, getType(mainType), regNo)

    private fun getDBMainType(type: AircraftType?) = when (type) {
        AircraftType.AIRPLANE -> type.toString()
        AircraftType.HELICOPTER -> type.toString()
        AircraftType.GLIDER -> type.toString()
        AircraftType.AUTOGYRO -> type.toString()
        AircraftType.AEROSTAT -> type.toString()
        AircraftType.AIRSHIP -> type.toString()
        else -> AircraftType.AIRPLANE.toString()
    }

    private fun getType(type: String?) = when (type) {
        "AIRPLANE" -> AircraftType.AIRPLANE
        "HELICOPTER" -> AircraftType.HELICOPTER
        "GLIDER" -> AircraftType.GLIDER
        "AUTOGYRO" -> AircraftType.AUTOGYRO
        "AEROSTAT" -> AircraftType.AEROSTAT
        "AIRSHIP" -> AircraftType.AIRSHIP
        else -> AircraftType.AIRPLANE
    }

    override fun addType(planeType: PlaneType): Long {
        return aircraftTypeDAO.insertReplace(planeType.toPlaneTypeEntity())
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