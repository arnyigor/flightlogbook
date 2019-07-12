package com.arny.flightlogbook.data.source

import com.arny.flightlogbook.data.db.daos.AircraftTypeDAO
import com.arny.flightlogbook.data.db.daos.FlightDAO
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.utils.OptionalNull
import io.reactivex.Observable

interface FlightsRepository {
    fun getFlightDAO(): FlightDAO
    fun getCraftTypeDAO(): AircraftTypeDAO
    fun getDbFlights(order: String): ArrayList<Flight> {
        return ArrayList(getFlightDAO().queryFlightsWithOrder(order))
    }

    fun updateFlight(flight: Flight): Observable<Boolean> {
        return Observable.fromCallable { getFlightDAO().updateReplace(flight) > 0 }
    }

    fun insertFlight(flight: Flight): Observable<Boolean> {
        return Observable.fromCallable { getFlightDAO().insertReplace(flight) > 0 }
    }

    fun insertFlights(flights: ArrayList<Flight>): Observable<Boolean> {
        return Observable.fromCallable { getFlightDAO().insertReplace(flights) }
                .map { it.any { it > 0 } }
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

    fun removeAllFlights(): Observable<Boolean> {
        return Observable.fromCallable { getFlightDAO().delete() > 0 }
    }

    fun removeFlight(id: Long): Observable<Boolean> {
        return Observable.fromCallable { getFlightDAO().delete(id) > 0 }
    }

}