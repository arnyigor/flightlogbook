package com.arny.flightlogbook.data.source

import android.content.Context
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.data.Local
import com.arny.flightlogbook.data.db.MainDB
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.base.BaseRepository
import io.reactivex.Observable

class MainRepositoryImpl : BaseRepository, MainRepository, DBRepository {
    private object Holder {
        val INSTANCE = MainRepositoryImpl()
    }

    companion object {
        val instance: MainRepositoryImpl by lazy { Holder.INSTANCE }
    }

    override fun getContext(): Context {
        return FlightApp.appContext
    }

    override fun getDb(): MainDB {
        return MainDB.getInstance(getContext())
    }

    override fun getDbTypeList(where: String?): Observable<List<AircraftType>>? {
        return Observable.fromCallable { Local.getTypeList(getContext()) }
    }

    override fun getFlight(id: Long): Observable<Flight> {
        return Observable.fromCallable { getDb().flightDAO.queryFlight(id) }
                .map {
                    val toLong = it.aircraft_id?.toLong() ?: 0
                    if (toLong != 0L) {
                        val type = getDb().aircraftTypeDAO.getType(toLong)
                        it.airplanetypetitle = type.typeName
                    }
                    it
                }
    }


}
