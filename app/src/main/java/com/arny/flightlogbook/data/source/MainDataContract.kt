package com.arny.flightlogbook.data.source

import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import io.reactivex.Observable

interface MainDataContract {
    fun getAllFlights(): Observable<List<Flight>>
    fun getFlight(id: Long): Observable<Flight>
    fun getDbTypeList(where: String? = null): Observable<List<AircraftType>>?
}