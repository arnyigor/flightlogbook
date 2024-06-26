package com.arny.flightlogbook.domain.planetypes

import com.arny.flightlogbook.data.models.PlaneType

interface AircraftTypesRepository {
    fun loadAircraftTypes(): List<PlaneType>
    fun loadAircraftType(id: Long?): PlaneType?
    fun loadPlaneTypeByRegNo(regNo: String?): PlaneType?
    fun addType(planeType: PlaneType): Long
    fun removeType(type: PlaneType?): Boolean
    fun removeTypes(): Boolean
    fun updateType(type: PlaneType): Boolean
    fun updatePlaneTypeTitle(id: Long?, title: String?): Boolean
    fun getAircraftTypesCount(): Int
}