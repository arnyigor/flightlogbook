package com.arny.domain.planetypes

import com.arny.domain.models.PlaneType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaneTypesInteractor @Inject constructor(private val planeTypesRepository: PlaneTypesRepository) {
    fun loadPlaneTypes(): List<PlaneType> = planeTypesRepository.loadPlaneTypes()

    fun loadPlaneType(id: Long?): PlaneType? = planeTypesRepository.loadPlaneType(id)

    fun addType(planeTypeId: Long? = null, name: String, regNo: String, type: AircraftType): Boolean {
        val planeType = PlaneType(planeTypeId, name, type, regNo)
        return if (planeTypeId == null) planeTypesRepository.addType(planeType) > 0L
        else planeTypesRepository.updateType(planeType)
    }

    fun removeType(item: PlaneType): Boolean = planeTypesRepository.removeType(item)
}
