package com.arny.flightlogbook.data.source

import com.arny.flightlogbook.data.models.Flight
import io.reactivex.Observable

interface MainDataContract {
    fun getAllFlights():Observable<List<Flight>>
    fun getFlight(id:Long):Observable<Flight>
}