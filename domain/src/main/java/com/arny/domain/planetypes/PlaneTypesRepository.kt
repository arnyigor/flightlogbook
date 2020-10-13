package com.arny.domain.planetypes

import com.arny.domain.models.PlaneType

interface PlaneTypesRepository {
    fun loadPlaneTypes(): List<PlaneType>
    fun loadPlaneType(id: Long?): PlaneType?
    fun loadPlaneTypeByRegNo(regNo: String?): PlaneType?
    fun addType(planeTypeId: Long?, name: String, regNo: String, type: AircraftType): Long
    fun addType(planeType: PlaneType): Long
    fun removeType(type: PlaneType?): Boolean
    fun removeTypes(): Boolean
    fun updateType(type: PlaneType?): Boolean
    fun updatePlaneTypeTitle(id: Long?, title: String?): Boolean
    fun getAircraftTypesCount(): Int
}