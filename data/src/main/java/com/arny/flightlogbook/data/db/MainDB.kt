package com.arny.flightlogbook.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.data.db.daos.*
import com.arny.flightlogbook.data.models.*

@Database(
        entities = [
            FlightEntity::class,
            PlaneTypeEntity::class,
            FlightTypeEntity::class,
            CustomFieldEntity::class,
            CustomFieldValueEntity::class
        ],
        version = CONSTS.DB.DB_VERSION,
        exportSchema = true
)
abstract class MainDB : RoomDatabase() {
    abstract val flightDAO: FlightDAO
    abstract val flightTypeDAO: FlightTypeDAO
    abstract val aircraftTypeDAO: AircraftTypeDAO
    abstract val customFieldDAO: CustomFieldDAO
    abstract val customFieldValuesDAO: CustomFieldValuesDAO
}
