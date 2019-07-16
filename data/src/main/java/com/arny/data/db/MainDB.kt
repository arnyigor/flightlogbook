package com.arny.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.arny.constants.CONSTS
import com.arny.data.db.daos.*
import com.arny.data.db.intities.TimeToFlightEntity
import com.arny.data.db.intities.TimeTypeEntity
import com.arny.data.models.*
import com.arny.helpers.utils.SingletonHolder

@Database(entities = [FlightEntity::class, PlaneTypeEntity::class, FlightTypeEntity::class, FlightTypeValueEntity::class, MigrationsEntity::class, TimeTypeEntity::class, TimeToFlightEntity::class], version = CONSTS.DB.DB_VERSION, exportSchema = false)
abstract class MainDB : RoomDatabase() {
    companion object : SingletonHolder<MainDB, Context>({
        Room.databaseBuilder(it.applicationContext, MainDB::class.java, CONSTS.DB.DB_NAME)
//                .fallbackToDestructiveMigration()
                .build()
    })
    abstract val flightDAO: FlightDAO
    abstract val flightTypeDAO: FlightTypeDAO
    abstract val aircraftTypeValueDAO: FlightTypeValueDAO
    abstract val aircraftTypeDAO: AircraftTypeDAO
    abstract val timeTypesDAO: TimeTypesDAO
    abstract val timeToFlightsDAO: TimeToFlightsDAO

}
