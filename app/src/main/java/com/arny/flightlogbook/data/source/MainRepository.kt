package com.arny.flightlogbook.data.source

import android.content.Context
import com.arny.arnylib.repository.BaseRepository
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.FlightApp.Companion.applicationComponent
import com.arny.flightlogbook.data.models.Flight
import io.reactivex.Observable

class MainRepository : BaseRepository(),MainDataContract {
    override fun getAllFlights(): Observable<List<Flight>> {
       return Observable.fromCallable { applicationComponent.getDb().flightDAO.flights }
    }

    override fun getFlight(id: Long): Observable<Flight> {
        return Observable.fromCallable { applicationComponent.getDb().flightDAO.getFlight(id) }
    }

    override fun getContext(): Context {
        return applicationComponent.getContext()
    }

    init {
        FlightApp.applicationComponent.inject(this)
    }




}
