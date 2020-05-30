package com.arny.data.db

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arny.data.R

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
}