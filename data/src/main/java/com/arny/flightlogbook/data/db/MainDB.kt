package com.arny.flightlogbook.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arny.core.CONSTS
import com.arny.flightlogbook.data.db.daos.*
import com.arny.flightlogbook.data.models.airports.AirportEntity
import com.arny.flightlogbook.data.models.customfields.CustomFieldEntity
import com.arny.flightlogbook.data.models.customfields.CustomFieldValueEntity
import com.arny.flightlogbook.data.models.flights.FlightEntity
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
        version = CONSTS.DB.DB_VERSION,
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
