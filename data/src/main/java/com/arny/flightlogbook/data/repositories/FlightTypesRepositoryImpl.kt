package com.arny.flightlogbook.data.repositories

import com.arny.flightlogbook.data.db.daos.FlightTypeDAO
import com.arny.flightlogbook.data.models.flights.FlightTypeEntity
import com.arny.flightlogbook.data.models.flights.toFlightTypeEntity
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository
import com.arny.flightlogbook.domain.models.FlightType
import javax.inject.Inject

class FlightTypesRepositoryImpl @Inject constructor(private val flightTypeDAO: FlightTypeDAO) : FlightTypesRepository {
    override fun loadDBFlightTypes(): List<FlightType> = flightTypeDAO.queryTypes().map { it.toFlightType() }

    override fun loadDBFlightType(id: Long?): FlightType? = flightTypeDAO.queryFlightType(id)?.toFlightType()

    override fun addFlightType(name: String): Boolean = FlightTypeEntity().let {
        it.typeTitle = name
        flightTypeDAO.insertReplace(it) > 0
    }

    override fun addFlightTypeAndGet(name: String): Long = FlightTypeEntity().let {
        it.typeTitle = name
        flightTypeDAO.insertReplace(it)
    }

    override fun addFlightTypeAndGet(type: FlightType): Long = flightTypeDAO.insertReplace(type.toFlightTypeEntity())

    override fun addFlightType(type: FlightType): Boolean = flightTypeDAO.insertReplace(type.toFlightTypeEntity()) > 0

    override fun addFlightTypes(types: List<FlightType>): Boolean =
        !flightTypeDAO.insertReplace(types.map { it.toFlightTypeEntity() }).contains(0)

    override fun removeFlightType(type: FlightType?): Boolean = flightTypeDAO.delete(type?.id) > 0

    override fun removeFlightType(id: Long?): Boolean = flightTypeDAO.delete(id) > 0

    override fun removeFlightTypes(): Boolean = flightTypeDAO.delete() > 0

    override fun updateFlightTypeTitle(id: Long?, title: String?): Boolean = flightTypeDAO.setTitle(id, title) > 0
}