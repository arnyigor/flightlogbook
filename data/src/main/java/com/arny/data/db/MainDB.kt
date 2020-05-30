package com.arny.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arny.constants.CONSTS
import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.db.daos.FlightDAO
import com.arny.data.db.daos.FlightTypeDAO
import com.arny.data.models.FlightEntity
import com.arny.data.models.FlightTypeEntity
import com.arny.data.models.PlaneTypeEntity

@Database(
        entities = [
            FlightEntity::class,
            PlaneTypeEntity::class,
            FlightTypeEntity::class
        ],
        version = CONSTS.DB.DB_VERSION,
        exportSchema = true
)
abstract class MainDB : RoomDatabase() {
    abstract val flightDAO: FlightDAO
    abstract val flightTypeDAO: FlightTypeDAO
    abstract val aircraftTypeDAO: AircraftTypeDAO

}
