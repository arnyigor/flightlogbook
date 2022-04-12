package com.arny.flightlogbook.domain.planetypes

import com.arny.flightlogbook.domain.models.PlaneType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaneTypesInteractor @Inject constructor(private val aircraftTypesRepository: AircraftTypesRepository) {
    fun loadPlaneTypes(): List<PlaneType> = aircraftTypesRepository.loadAircraftTypes()

    fun loadPlaneType(id: Long?): PlaneType? = aircraftTypesRepository.loadAircraftType(id)

    fun addType(planeTypeId: Long? = null, name: String, regNo: String, type: AircraftType): Boolean {
        val planeType = PlaneType(planeTypeId, name, type, regNo)
        return if (planeTypeId == null) aircraftTypesRepository.addType(planeType) > 0L
        else aircraftTypesRepository.updateType(planeType)
    }

    fun removeType(item: PlaneType): Boolean = aircraftTypesRepository.removeType(item)
}
