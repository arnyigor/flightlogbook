package com.arny.flightlogbook.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.arny.flightlogbook.data.models.Flight

@Database(entities = [(Flight::class)], version = 1, exportSchema = false)
abstract class MainDB : RoomDatabase() {
    abstract val flightDAO: FlightDAO

}
