package com.arny.data.repositories

import android.content.Context
import com.arny.data.db.MainDB
import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.db.daos.FlightDAO
import com.arny.data.db.daos.TimeToFlightsDAO
import com.arny.data.db.daos.TimeTypesDAO
import com.arny.data.repositories.base.BaseRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MainRepositoryImpl @Inject constructor(private val appContext: Context) : BaseRepository, FlightsRepository, TypesRepository, TimesRepository {

    override fun getFlightDAO(): FlightDAO {
        return getDb().flightDAO
    }

    override fun getTimeTypeDAO(): TimeTypesDAO {
         return getDb().timeTypesDAO
    }

    override fun getTimeToFlightsDAO(): TimeToFlightsDAO {
         return getDb().timeToFlightsDAO
    }

    override fun getCraftTypeDAO(): AircraftTypeDAO {
        return getDb().aircraftTypeDAO
    }

    override fun getContext(): Context {
        return appContext
    }

    private fun getDb(): MainDB {
        return MainDB.getInstance(getContext())
    }

}
