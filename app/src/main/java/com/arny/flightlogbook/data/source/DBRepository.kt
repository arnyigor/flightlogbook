package com.arny.flightlogbook.data.source

import com.arny.flightlogbook.data.db.AircraftTypeDAO
import com.arny.flightlogbook.data.db.FlightDAO
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.utils.OptionalNull
import io.reactivex.Observable

interface DBRepository {
    fun getFlightDAO(): FlightDAO
    fun getCraftTypeDAO(): AircraftTypeDAO
    fun getDbFlights(order: String): Observable<ArrayList<Flight>> {
        return Observable.fromCallable { getFlightDAO().queryFlightsWithOrder(order) }.map { it -> it as ArrayList<Flight> }
    }

    fun getFlight(id: Long): Observable<OptionalNull<Flight>> {
        return Observable.fromCallable { OptionalNull(getFlightDAO().queryFlight(id)) }
                .map {
                    val flight = it.value
                    if (flight != null) {
                        val nullable = OptionalNull(getCraftTypeDAO().queryAircraftType(flight.aircraft_id?.toLong()
                                ?: 0))
                        val type = nullable.value
                        if (type != null) {
                            flight.airplanetypetitle = type.typeName
                        }
                    }
                    it
                }
    }

    fun getAircraftTypes(): Observable<ArrayList<AircraftType>> {
        return Observable.fromCallable { getCraftTypeDAO().queryAircraftTypes() }.map { it -> it as ArrayList<AircraftType> }
    }

    fun getFlightsTime(): Observable<Int> {
        return Observable.fromCallable { getFlightDAO().queryFlightTime() }
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
        return Observable.fromCallable { getFlightDAO().queryFlightsCount() }
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
        return Observable.fromCallable { getCraftTypeDAO().queryAirplaneTypesCount() }
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
        return Observable.fromCallable { getFlightDAO().delete() > 0 }
    }

    fun removeFlight(id: Long): Observable<Boolean> {
        return Observable.fromCallable { getFlightDAO().delete(id) > 0 }
    }

}