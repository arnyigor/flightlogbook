package com.arny.flightlogbook.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arny.flightlogbook.data.db.daos.*
import com.arny.flightlogbook.data.db.intity.airports.AirportEntity
import com.arny.flightlogbook.data.db.intity.customfields.CustomFieldEntity
import com.arny.flightlogbook.data.db.intity.customfields.CustomFieldValueEntity
import com.arny.flightlogbook.data.db.intity.flights.FlightEntity
import com.arny.flightlogbook.data.models.flights.FlightTypeEntity
import com.arny.flightlogbook.data.models.planes.PlaneTypeEntity

@Database(
    entities = [
        FlightEntity::class,
        PlaneTypeEntity::class,
        FlightTypeEntity::class,
        CustomFieldEntity::class,
        CustomFieldValueEntity::class,
        AirportEntity::class,
    ],
//    autoMigrations = [
//        AutoMigration(from = 14, to = 15),
//    ],
    version = 14,
    exportSchema = true
)
abstract class MainDB : RoomDatabase() {

    abstract val flightDAO: FlightDAO
    abstract val flightTypeDAO: FlightTypeDAO
    abstract val aircraftTypeDAO: AircraftTypeDAO
    abstract val customFieldDAO: CustomFieldDAO
    abstract val customFieldValuesDAO: CustomFieldValuesDAO
    abstract val airportsDAO: AirportsDAO
}
