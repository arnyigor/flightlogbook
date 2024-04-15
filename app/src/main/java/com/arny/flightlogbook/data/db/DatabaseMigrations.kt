package com.arny.flightlogbook.data.db

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arny.flightlogbook.data.utils.readAssetFile

class DatabaseMigrations(val context: Context) {
    fun getMigration12To13(): Migration = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.beginTransaction()
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `main_table_new` (`date` TEXT, `datetime` INTEGER, `log_time` INTEGER, `ground_time` INTEGER, `night_time` INTEGER, `reg_no` TEXT, `airplane_type` INTEGER, `day_night` INTEGER, `ifr_vfr` INTEGER, `flight_type` INTEGER, `description` TEXT, `_id` INTEGER PRIMARY KEY AUTOINCREMENT)"
            )
            db.execSQL(
                "INSERT INTO main_table_new(date, datetime, log_time, reg_no, airplane_type, day_night, ifr_vfr, flight_type, description) SELECT date, datetime, log_time, reg_no, airplane_type, day_night, ifr_vfr, flight_type, description FROM main_table"
            )
            db.execSQL("DROP TABLE main_table")
            db.execSQL("ALTER TABLE main_table_new RENAME TO main_table")
            db.execSQL("CREATE TABLE IF NOT EXISTS `flight_type` (`title` TEXT, `_id` INTEGER PRIMARY KEY AUTOINCREMENT)")
            db.execSQL("INSERT INTO flight_type(title,_id) VALUES('Circuits','0')")
            db.execSQL("INSERT INTO flight_type(title,_id) VALUES('Zone','1')")
            db.execSQL("INSERT INTO flight_type(title,_id) VALUES('Route','2')")
            db.execSQL("ALTER TABLE main_table ADD COLUMN params TEXT")
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }

    fun getMigration13To14(): Migration = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.beginTransaction()
            db.execSQL("CREATE TABLE IF NOT EXISTS `airports` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, `icao` TEXT, `iata` TEXT, `name_rus` TEXT, `name_eng` TEXT, `city_rus` TEXT, `city_eng` TEXT, `country_rus` TEXT, `country_eng` TEXT, `latitude` REAL, `longitude` REAL, `elevation` REAL)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_airports_icao_iata ON airports (icao, iata)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `custom_fields` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `type` TEXT, `add_time` INTEGER NOT NULL, `show_by_default` INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `custom_field_values` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, `externalId` INTEGER, `type` TEXT, `value` TEXT, `fieldId` INTEGER, CONSTRAINT fk_custom_fields FOREIGN KEY (fieldId) REFERENCES custom_fields(_id) ON DELETE CASCADE, CONSTRAINT fk_main_table FOREIGN KEY (externalId) REFERENCES main_table(_id) ON DELETE CASCADE)")
            db.execSQL("ALTER TABLE type_table ADD COLUMN `main_type` TEXT")
            db.execSQL("ALTER TABLE type_table ADD COLUMN `reg_no` TEXT")
            db.execSQL("ALTER TABLE main_table ADD COLUMN `departure_id` INTEGER")
            db.execSQL("ALTER TABLE main_table ADD COLUMN `arrival_id` INTEGER")
            db.execSQL("ALTER TABLE main_table ADD COLUMN `departure_utc_time` TEXT")
            db.execSQL("ALTER TABLE main_table ADD COLUMN `arrival_utc_time` TEXT")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_type_table_reg_no ON type_table (reg_no)")
            db.setTransactionSuccessful()
            db.endTransaction()
            runMigration(db, context.readAssetFile("migrations", "airports.sql"))
        }
    }

    private fun runMigration(database: SupportSQLiteDatabase, sql: String?) {
        if (sql != null) {
            val sqlList = sql.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            database.beginTransaction()
            try {
                for (s in sqlList) {
                    if (s.isNotBlank()) {
                        database.execSQL(s)
                    }
                }
                database.setTransactionSuccessful()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            database.endTransaction()
        }
    }

    fun onCreateCallback(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("PRAGMA encoding='UTF-8';")
                runMigration(
                    db,
                    context.readAssetFile("migrations", "airports.sql")
                )
            }
        }
    }
}