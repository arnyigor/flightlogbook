package com.arny.flightlogbook.data.source

import android.content.Context
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.data.db.AircraftTypeDAO
import com.arny.flightlogbook.data.db.FlightDAO
import com.arny.flightlogbook.data.db.MainDB
import com.arny.flightlogbook.data.source.base.BaseRepository

class MainRepositoryImpl : BaseRepository, MainRepository, DBRepository {
    private object Holder {
        val INSTANCE = MainRepositoryImpl()
    }

    companion object {
        val instance: MainRepositoryImpl by lazy { Holder.INSTANCE }
    }

    override fun getFlightDAO(): FlightDAO {
        return getDb().flightDAO
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
