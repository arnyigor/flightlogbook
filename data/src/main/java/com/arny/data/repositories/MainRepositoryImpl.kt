package com.arny.data.repositories

import android.content.Context
import com.arny.data.db.MainDB
import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.db.daos.FlightDAO
import com.arny.data.db.daos.FlightTypeDAO
import com.arny.data.repositories.base.BaseRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MainRepositoryImpl @Inject constructor(private val appContext: Context) : BaseRepository, FlightsRepository, PlaneTypesRepository, FlightTypesRepository {

    override fun getFlightDAO(): FlightDAO {
        return getDb().flightDAO
    }

    override fun getFlightTypeDAO(): FlightTypeDAO {
        return getDb().flightTypeDAO
    }

    override fun getPlaneTypeDAO(): AircraftTypeDAO {
        return getDb().aircraftTypeDAO
    }

    override fun getContext(): Context {
        return appContext
    }

    override fun getDb(): MainDB {
        return MainDB.getInstance(getContext())
    }
}
