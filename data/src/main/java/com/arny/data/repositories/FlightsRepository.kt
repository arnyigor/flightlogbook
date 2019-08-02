package com.arny.data.repositories

import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.db.daos.FlightDAO
import com.arny.data.models.FlightEntity

interface FlightsRepository:BaseDBRepository {
    fun getFlightDAO(): FlightDAO
    fun getPlaneTypeDAO(): AircraftTypeDAO
    fun getDbFlights(order: String): ArrayList<FlightEntity> {
        return ArrayList(getFlightDAO().queryFlightsWithOrder(order))
    }

    fun updateFlight(flight: FlightEntity): Boolean {
        return getFlightDAO().updateReplace(flight) > 0
    }

    fun insertFlight(flight: FlightEntity): Boolean {
        return getFlightDAO().insertReplace(flight) > 0
    }

    fun insertFlights(flights: List<FlightEntity>): Boolean {
        return getFlightDAO().insertReplace(flights).any { it>0 }
    }

    fun getFlight(id: Long?): FlightEntity? {
        val flightEntity = getFlightDAO().queryFlight(id)
        flightEntity?.let {flight->
            val aircraftType = getPlaneTypeDAO().queryAircraftType(flight.aircraft_id ?: 0)
            flight.airplanetypetitle = aircraftType?.typeName
            aircraftType
        }
        return flightEntity
    }

    fun getFlightsTime(): Int {
        val cursor = getFlightDAO().queryFlightTime()
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            cursor.close()
        }
        return count
    }

    fun getFlightsCount(): Int {
        val cursor = getFlightDAO().queryFlightsCount()
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            cursor.close()
        }
        return count
    }

    fun removeAllFlights(): Boolean {
        return getFlightDAO().delete() > 0
    }

    fun removeFlight(id: Long?): Boolean {
        return getFlightDAO().delete(id) > 0
    }

}