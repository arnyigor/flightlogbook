package com.arny.flightlogbook.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
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
    autoMigrations = [
        AutoMigration(from = 14, to = 15, spec = MainDB.FlightDepArrMigration::class),
    ],
    version = 15,
    exportSchema = true
)
abstract class MainDB : RoomDatabase() {
    @RenameColumn.Entries(
        RenameColumn(
            tableName = "main_table",
            fromColumnName = "departure_utc_time",
            toColumnName = "departure_local_time"
        ),
        RenameColumn(
            tableName = "main_table",
            fromColumnName = "arrival_utc_time",
            toColumnName = "arrival_local_time"
        ),
    )
    class FlightDepArrMigration : AutoMigrationSpec

    abstract val flightDAO: FlightDAO
    abstract val flightTypeDAO: FlightTypeDAO
    abstract val aircraftTypeDAO: AircraftTypeDAO
    abstract val customFieldDAO: CustomFieldDAO
    abstract val customFieldValuesDAO: CustomFieldValuesDAO
    abstract val airportsDAO: AirportsDAO
}
