package com.arny.flightlogbook.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.models.*
import com.arny.flightlogbook.utils.SingletonHolder

@Database(entities = [Flight::class, PlaneType::class, FlightType::class, FlightTypeValue::class, Migrations::class], version = Consts.DB.DB_VERSION, exportSchema = false)
abstract class MainDB : RoomDatabase() {
    companion object : SingletonHolder<MainDB, Context>({
        Room.databaseBuilder(it.applicationContext, MainDB::class.java, Consts.DB.DB_NAME)
//                .fallbackToDestructiveMigration()
                .build()
    })
    abstract val flightDAO: FlightDAO
    abstract val flightTypeDAO: FlightTypeDAO
    abstract val aircraftTypeValueDAO: FlightTypeValueDAO
    abstract val aircraftTypeDAO: AircraftTypeDAO

}
