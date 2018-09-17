package com.arny.flightlogbook.data.source

import com.arny.flightlogbook.data.db.MainDB
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import io.reactivex.Observable

interface DBRepository {
    fun getDb(): MainDB
    fun getDbFlights(order: String): Observable<ArrayList<Flight>>? {
        return Observable.fromCallable { getDb().flightDAO.queryFlightsWithOrder(order) }.map { it -> it as ArrayList<Flight> }
    }

    fun getFlight(id: Long): Observable<Flight> {
        return Observable.fromCallable { getDb().flightDAO.queryFlight(id) }
                .map {
                    val type = getDb().aircraftTypeDAO.queryAircraftType(it.aircraft_id?.toLong()
                            ?: 0)
                    it.airplanetypetitle = type.typeName
                    it
                }
    }

    fun getAircraftTypes(): Observable<ArrayList<AircraftType>>? {
        return Observable.fromCallable { getDb().aircraftTypeDAO.queryAircraftTypes() }.map { it -> it as ArrayList<AircraftType> }
    }

    fun getFlightsTime(): Observable<Int> {
        return Observable.fromCallable { getDb().flightDAO.queryFlightTime() }
                .map { cursor ->
                    var count = 0
                    if (cursor.moveToFirst()) {
                        count = cursor.getInt(0)
                        cursor.close()
                    }
                    count
                }
    }

    fun getFlightsCount(): Observable<Int> {
        return Observable.fromCallable { getDb().flightDAO.queryFlightsCount() }
                .map { cursor ->
                    var count = 0
                    if (cursor.moveToFirst()) {
                        count = cursor.getInt(0)
                        cursor.close()
                    }
                    count
                }
    }

    fun getAircraftTypesCount(): Observable<Int> {
        return Observable.fromCallable { getDb().aircraftTypeDAO.queryAirplaneTypesCount() }
                .map { cursor ->
                    var count = 0
                    if (cursor.moveToFirst()) {
                        count = cursor.getInt(0)
                        cursor.close()
                    }
                    count
                }
    }

    fun removeAllFlights(): Observable<Boolean> {
        return Observable.fromCallable { getDb().flightDAO.delete() > 0 }
    }

    fun removeFlight(id: Long): Observable<Boolean> {
        return Observable.fromCallable { getDb().flightDAO.delete(id) > 0 }
    }
}