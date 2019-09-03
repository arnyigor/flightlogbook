package com.arny.data.repositories

import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.db.daos.FlightDAO
import com.arny.data.models.FlightEntity
import com.arny.helpers.utils.DBProvider
import com.arny.helpers.utils.toItem

interface FlightsRepository:BaseDBRepository {
    fun getFlightDAO(): FlightDAO
    fun getPlaneTypeDAO(): AircraftTypeDAO
    fun getDbFlights(order: String): ArrayList<FlightEntity> {
        return ArrayList(getFlightDAO().queryFlightsWithOrder(order))
    }

    fun getDbFlights( ): ArrayList<FlightEntity> {
        return ArrayList(getFlightDAO().queryFlights())
    }

    fun getStatisticDbFlights(startDate: Long, endDate: Long, includeEnd: Boolean): ArrayList<FlightEntity> {
        if (includeEnd) {
            return ArrayList(getFlightDAO().queryFlightsInclude(startDate,endDate))
        }
        return ArrayList(getFlightDAO().queryFlights(startDate,endDate))
    }

    fun getStatisticDbFlightsByFlightTypes(startDate: Long, endDate: Long, flightTypes: List<Long?>, includeEnd: Boolean): ArrayList<FlightEntity> {
        if (includeEnd) {
            return ArrayList(getFlightDAO().queryFlightsByFlightTypesInclude(startDate,endDate,flightTypes))
        }
        return ArrayList(getFlightDAO().queryFlightsByFlightTypes(startDate,endDate,flightTypes))
    }

    fun getStatisticDbFlightsByPlanes(startDate: Long, endDate: Long, planeTypes: List<Long?>, includeEnd: Boolean): ArrayList<FlightEntity> {
        if (includeEnd) {
            return  ArrayList(getFlightDAO().queryFlightsByPlanesIncludeEnd(startDate,endDate,planeTypes))
        }
        return ArrayList(getFlightDAO().queryFlightsByPlanes(startDate,endDate,planeTypes))
    }

    fun getStatisticDbFlightsMinMax(): Pair<Long, Long> {
        var min = 0L
        var max = 0L
        getFlightDAO().queryMinMaxDateTimes().toItem {
             min = DBProvider.getCursorLong(it, "first")
            max = DBProvider.getCursorLong(it, "last")
        }
        return Pair(min,max)
    }

    fun updateFlight(flight: FlightEntity): Boolean {
        return getFlightDAO().updateReplace(flight) > 0
    }

    fun insertFlight(flight: FlightEntity): Boolean {
        return getFlightDAO().insertReplace(flight) > 0
    }


    fun insertFlightAndGet(flight: FlightEntity): Long {
        return getFlightDAO().insertReplace(flight)
    }

    fun insertFlights(flights: List<FlightEntity>): Boolean {
        return getFlightDAO().insertReplace(flights).any { it>0 }
    }

    fun getFlight(id: Long?): FlightEntity? {
        val flightEntity = getFlightDAO().queryFlight(id)
        flightEntity?.let {flight->
            val aircraftType = getPlaneTypeDAO().queryAircraftType(flight.planeId ?: 0)
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