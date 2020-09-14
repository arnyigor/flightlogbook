package com.arny.domain.planetypes

import com.arny.domain.models.PlaneType

interface PlaneTypesRepository {
    fun loadPlaneTypes(): List<PlaneType>
    fun loadPlaneType(id: Long?): PlaneType?
    fun addType(name: String): Boolean
    fun addTypeAndGet(name: String): Long
    fun removeType(type: PlaneType?): Boolean
    fun removeTypes(): Boolean
    fun updateType(type: PlaneType?): Boolean
    fun updatePlaneTypeTitle(id: Long?, title: String?): Boolean
    fun getAircraftTypesCount(): Int
}