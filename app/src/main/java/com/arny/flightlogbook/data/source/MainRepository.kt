package com.arny.flightlogbook.data.source

import android.content.Context
import com.arny.arnylib.repository.BaseRepository
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.FlightApp.Companion.applicationComponent
import com.arny.flightlogbook.data.Local
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import io.reactivex.Observable

class MainRepository : BaseRepository(), MainDataContract {
    override fun getDbTypeList(where: String?): Observable<List<AircraftType>>? {
        return Observable.fromCallable { Local.getTypeList(getContext()) }
    }

    override fun getAllFlights(): Observable<List<Flight>> {
        return Observable.fromCallable { applicationComponent.getDb().flightDAO.flights }
    }

    override fun getFlight(id: Long): Observable<Flight> {
        val db = applicationComponent.getDb()
        return Observable.fromCallable { db.flightDAO.getFlight(id) }
                .map {
                    val type = db.flightTypeDAO.getFlightType(it.airplanetypeid.toLong())
                    it.airplanetypetitle = type.typeTitle
                    it
                }
    }

    override fun getContext(): Context {
        return applicationComponent.getContext()
    }

    init {
        FlightApp.applicationComponent.inject(this)
    }

}
