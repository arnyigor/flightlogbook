package com.arny.flightlogbook.data.source

import android.content.Context
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.data.db.MainDB
import com.arny.flightlogbook.data.source.base.BaseRepository

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

}
