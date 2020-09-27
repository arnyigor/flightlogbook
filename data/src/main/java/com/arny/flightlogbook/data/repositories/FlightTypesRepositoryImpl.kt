package com.arny.flightlogbook.data.repositories

import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.data.db.daos.FlightTypeDAO
import com.arny.flightlogbook.data.models.flights.FlightTypeEntity
import com.arny.flightlogbook.data.models.flights.toFlightTypeEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightTypesRepositoryImpl @Inject constructor(private val flightTypeDAO: FlightTypeDAO) : FlightTypesRepository {
    override fun loadDBFlightTypes(): List<FlightType> {
        return flightTypeDAO.queryTypes().map { it.toFlightType() }
    }

    override fun loadDBFlightType(id: Long?): FlightType? {
        return flightTypeDAO.queryFlightType(id)?.toFlightType()
    }

    override fun addFlightType(name: String): Boolean {
        val type = FlightTypeEntity()
        type.typeTitle = name
        return flightTypeDAO.insertReplace(type) > 0
    }

    override fun addFlightTypeAndGet(name: String): Long {
        val type = FlightTypeEntity()
        type.typeTitle = name
        return flightTypeDAO.insertReplace(type)
    }

    override fun addFlightType(type: FlightType): Boolean {
        val typeEntity = type.toFlightTypeEntity()
        return flightTypeDAO.insertReplace(typeEntity) > 0
    }

    override fun addFlightTypes(types: List<FlightType>): Boolean {
        val items = types.map { it.toFlightTypeEntity() }
        val ids = flightTypeDAO.insertReplace(items)
        return !ids.contains(0)
    }

    override fun removeFlightType(type: FlightType?): Boolean {
        return flightTypeDAO.delete(type?.id) > 0
    }

    override fun removeFlightType(id: Long?): Boolean {
        return flightTypeDAO.delete(id) > 0
    }

    override fun removeFlightTypes(): Boolean {
        return flightTypeDAO.delete() > 0
    }

    override fun updateFlightTypeTitle(id: Long?, title: String?): Boolean {
        return flightTypeDAO.setTitle(id, title) > 0
    }
}