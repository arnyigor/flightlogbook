package com.arny.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arny.constants.CONSTS
import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.db.daos.FlightDAO
import com.arny.data.db.daos.FlightTypeDAO
import com.arny.data.db.daos.FlightTypeValueDAO
import com.arny.data.models.*
import com.arny.helpers.utils.SingletonHolder

@Database(entities = [FlightEntity::class, PlaneTypeEntity::class, FlightTypeEntity::class, FlightTypeValueEntity::class, MigrationsEntity::class], version = CONSTS.DB.DB_VERSION, exportSchema = false)
abstract class MainDB : RoomDatabase() {
    companion object : SingletonHolder<MainDB, Context>({
        Room.databaseBuilder(it.applicationContext, MainDB::class.java, CONSTS.DB.DB_NAME)
                .build()
    })

    abstract val flightDAO: FlightDAO
    abstract val flightTypeDAO: FlightTypeDAO
    abstract val aircraftTypeValueDAO: FlightTypeValueDAO
    abstract val aircraftTypeDAO: AircraftTypeDAO

}
