package com.arny.domain.flighttypes

import com.arny.domain.models.FlightType

interface FlightTypesRepository {
    fun loadDBFlightTypes(): List<FlightType>
    fun loadDBFlightType(id: Long?): FlightType?
    fun addFlightType(name: String): Boolean
    fun addFlightTypeAndGet(name: String): Long
    fun addFlightType(type: FlightType): Boolean
    fun addFlightTypes(types: List<FlightType>): Boolean
    fun removeFlightType(type: FlightType?): Boolean
    fun removeFlightType(id: Long?): Boolean
    fun removeFlightTypes(): Boolean
    fun updateFlightTypeTitle(id: Long?, title: String?): Boolean
}
