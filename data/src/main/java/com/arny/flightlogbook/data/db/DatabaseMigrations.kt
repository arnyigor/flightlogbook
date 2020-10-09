package com.arny.flightlogbook.data.db

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arny.flightlogbook.data.R
import com.arny.flightlogbook.data.utils.DBUtils
import com.arny.helpers.utils.Utility

class DatabaseMigrations(val context: Context) {
    fun getMigration12To13(): Migration = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.beginTransaction()
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `main_table_new` (`date` TEXT, `datetime` INTEGER, `log_time` INTEGER, `ground_time` INTEGER, `night_time` INTEGER, `reg_no` TEXT, `airplane_type` INTEGER, `day_night` INTEGER, `ifr_vfr` INTEGER, `flight_type` INTEGER, `description` TEXT, `_id` INTEGER PRIMARY KEY AUTOINCREMENT)"
            )
            database.execSQL(
                    "INSERT INTO main_table_new(date, datetime, log_time, reg_no, airplane_type, day_night, ifr_vfr, flight_type, description) SELECT date, datetime, log_time, reg_no, airplane_type, day_night, ifr_vfr, flight_type, description FROM main_table"
            )
            database.execSQL("DROP TABLE main_table")
            database.execSQL("ALTER TABLE main_table_new RENAME TO main_table")
            database.execSQL("CREATE TABLE IF NOT EXISTS `flight_type` (`title` TEXT, `_id` INTEGER PRIMARY KEY AUTOINCREMENT)")
            database.execSQL("INSERT INTO flight_type(title,_id) VALUES('${context.resources.getString(R.string.flight_type_circle)}','0')")
            database.execSQL("INSERT INTO flight_type(title,_id) VALUES('${context.resources.getString(R.string.flight_type_zone)}','1')")
            database.execSQL("INSERT INTO flight_type(title,_id) VALUES('${context.resources.getString(R.string.flight_type_route)}','2')")
            database.execSQL("ALTER TABLE main_table ADD COLUMN params TEXT")
            database.setTransactionSuccessful()
            database.endTransaction()
        }
    }

    fun getMigration13To14(): Migration = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.beginTransaction()
            database.execSQL("CREATE TABLE IF NOT EXISTS `airports` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, `icao` TEXT, `iata` TEXT, `name_rus` TEXT, `name_eng` TEXT, `city_rus` TEXT, `city_eng` TEXT, `country_rus` TEXT, `country_eng` TEXT, `latitude` REAL, `longitude` REAL, `elevation` REAL)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_airports_icao_iata ON airports (icao, iata)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `custom_fields` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `type` TEXT, `add_time` INTEGER NOT NULL, `show_by_default` INTEGER NOT NULL)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `custom_field_values` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, `externalId` INTEGER, `type` TEXT, `value` TEXT, `fieldId` INTEGER, CONSTRAINT fk_custom_fields FOREIGN KEY (fieldId) REFERENCES custom_fields(_id) ON DELETE CASCADE, CONSTRAINT fk_main_table FOREIGN KEY (externalId) REFERENCES main_table(_id) ON DELETE CASCADE)")
            database.execSQL("ALTER TABLE type_table ADD COLUMN `main_type` TEXT")
            database.execSQL("ALTER TABLE type_table ADD COLUMN `reg_no` TEXT")
            database.execSQL("ALTER TABLE main_table ADD COLUMN `departure_id` INTEGER")
            database.execSQL("ALTER TABLE main_table ADD COLUMN `arrival_id` INTEGER")
            database.execSQL("ALTER TABLE main_table ADD COLUMN `departure_utc_time` TEXT")
            database.execSQL("ALTER TABLE main_table ADD COLUMN `arrival_utc_time` TEXT")
            database.execSQL("ALTER TABLE main_table ADD COLUMN `title` TEXT")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_type_table_airplane_type_reg_no ON type_table (airplane_type,reg_no)")
            database.setTransactionSuccessful()
            database.endTransaction()
            DBUtils.runMigration(database, Utility.readAssetFile(context, "migrations", "airports.sql"))
        }
    }

    fun onCreateCallback(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DBUtils.runMigration(db, Utility.readAssetFile(context, "migrations", "airports.sql"))
            }
        }
    }
}