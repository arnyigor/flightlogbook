package com.arny.flightlogbook.domain.flighttypes

import com.arny.flightlogbook.data.models.FlightType

interface FlightTypesRepository {
    fun loadDBFlightTypes(): List<FlightType>
    fun loadDBFlightType(id: Long?): FlightType?
    fun addFlightType(name: String): Boolean
    fun addFlightTypeAndGet(name: String): Long
    fun addFlightTypeAndGet(type: FlightType): Long
    fun addFlightType(type: FlightType): Boolean
    fun addFlightTypes(types: List<FlightType>): Boolean
    fun removeFlightType(type: FlightType?): Boolean
    fun removeFlightType(id: Long?): Boolean
    fun removeFlightTypes(): Boolean
    fun updateFlightTypeTitle(id: Long?, title: String?): Boolean
}
