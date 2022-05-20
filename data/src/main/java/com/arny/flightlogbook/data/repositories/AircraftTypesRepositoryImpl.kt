package com.arny.flightlogbook.data.repositories

import com.arny.flightlogbook.data.db.daos.AircraftTypeDAO
import com.arny.flightlogbook.data.models.planes.PlaneTypeEntity
import com.arny.flightlogbook.domain.models.PlaneType
import com.arny.flightlogbook.domain.planetypes.AircraftType
import com.arny.flightlogbook.domain.planetypes.AircraftTypesRepository
import javax.inject.Inject

class AircraftTypesRepositoryImpl @Inject constructor(
    private val aircraftTypeDAO: AircraftTypeDAO
) : AircraftTypesRepository {
    override fun loadAircraftTypes(): List<PlaneType> =
        aircraftTypeDAO.queryAircraftTypes().map { it.toPlaneType() }

    override fun loadAircraftType(id: Long?): PlaneType? =
        aircraftTypeDAO.queryAircraftType(id)?.toPlaneType()

    override fun loadPlaneTypeByRegNo(regNo: String?): PlaneType? =
        aircraftTypeDAO.queryAircraftByRegNo(regNo)?.toPlaneType()

    private fun PlaneType.toPlaneTypeEntity(): PlaneTypeEntity = PlaneTypeEntity(
        typeId = typeId,
        typeName = typeName,
        regNo = regNo,
        mainType = getDBMainType(mainType ?: AircraftType.AIRPLANE)
    )

    private fun PlaneTypeEntity.toPlaneType() = PlaneType(
        typeId = typeId,
        typeName = typeName,
        mainType = getType(mainType),
        regNo = regNo
    )

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

    override fun addType(planeType: PlaneType): Long =
        aircraftTypeDAO.insertReplace(planeType.toPlaneTypeEntity())

    override fun removeType(type: PlaneType?): Boolean = aircraftTypeDAO.delete(type?.typeId) > 0

    override fun removeTypes(): Boolean = aircraftTypeDAO.delete() > 0

    override fun updateType(type: PlaneType): Boolean =
        aircraftTypeDAO.updateReplace(type.toPlaneTypeEntity()) > 0

    override fun updatePlaneTypeTitle(id: Long?, title: String?): Boolean =
        aircraftTypeDAO.setTitle(id, title) > 0

    override fun getAircraftTypesCount(): Int {
        var count = 0
        aircraftTypeDAO.queryAirplaneTypesCount().use { cursor ->
            if (cursor.moveToNext()) {
                count = cursor.getInt(0)
            }
        }
        return count
    }

}