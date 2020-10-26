package com.arny.domain.planetypes

import com.arny.domain.models.PlaneType

interface AircraftTypesRepository {
    fun loadAircraftNames(): List<PlaneType>
    fun loadAircraftTypes(): List<PlaneType>
    fun loadAircraftType(id: Long?): PlaneType?
    fun loadPlaneTypeByRegNo(regNo: String?): PlaneType?
    fun addType(planeType: PlaneType): Long
    fun removeType(type: PlaneType?): Boolean
    fun removeTypes(): Boolean
    fun updateType(type: PlaneType?): Boolean
    fun updatePlaneTypeTitle(id: Long?, title: String?): Boolean
    fun getAircraftTypesCount(): Int
}