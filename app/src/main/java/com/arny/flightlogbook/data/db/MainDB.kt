package com.arny.flightlogbook.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.models.FlightType

@Database(entities = [Flight::class, AircraftType::class, FlightType::class], version = 1, exportSchema = false)
abstract class MainDB : RoomDatabase() {
    abstract val flightDAO: FlightDAO
    abstract val flightTypeDAO: FlightTypeDAO

}
