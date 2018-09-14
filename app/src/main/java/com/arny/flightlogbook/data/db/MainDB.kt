package com.arny.flightlogbook.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.models.FlightType
import com.arny.flightlogbook.data.models.Migrations
import com.arny.flightlogbook.utils.SingletonHolder

@Database(entities = [Flight::class, AircraftType::class, FlightType::class, Migrations::class], version = Consts.DB.DB_VERSION, exportSchema = false)
abstract class MainDB : RoomDatabase() {
    companion object : SingletonHolder<MainDB, Context>({
        Room.databaseBuilder(it.applicationContext,
                MainDB::class.java, Consts.DB.DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    })
    abstract val flightDAO: FlightDAO
//    abstract val flightTypeDAO: FlightTypeDAO
    abstract val aircraftTypeDAO: AircraftTypeDAO

}
