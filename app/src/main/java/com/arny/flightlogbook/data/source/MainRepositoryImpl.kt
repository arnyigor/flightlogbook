package com.arny.flightlogbook.data.source

import android.content.Context
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.data.db.MainDB
import com.arny.flightlogbook.data.db.daos.AircraftTypeDAO
import com.arny.flightlogbook.data.db.daos.FlightDAO
import com.arny.flightlogbook.data.db.daos.TimeTypeDAO
import com.arny.flightlogbook.data.source.base.BaseRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MainRepositoryImpl @Inject constructor() : BaseRepository, FlightsRepository, TypesRepository, TimeRepository {

    override fun getFlightDAO(): FlightDAO {
        return getDb().flightDAO
    }

    override fun getTimeTipeDAO(): TimeTypeDAO {
         return getDb().timeTypeDAO
    }

    override fun getCraftTypeDAO(): AircraftTypeDAO {
        return getDb().aircraftTypeDAO
    }

    override fun getContext(): Context {
        return FlightApp.appContext
    }

    private fun getDb(): MainDB {
        return MainDB.getInstance(getContext())
    }

}
