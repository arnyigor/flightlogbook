package com.arny.data.db.daos

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseMigrations {
    companion object{
        val MIGRATION_12_13: Migration = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    BEGIN TRANSACTION;
                    CREATE TABLE IF NOT EXISTS `main_table_new` (`date` TEXT, `datetime` INTEGER, `log_time` INTEGER, `ground_time` INTEGER, `night_time` INTEGER, `reg_no` TEXT, `airplane_type` INTEGER, `day_night` INTEGER, `ifr_vfr` INTEGER, `flight_type` INTEGER, `description` TEXT, `title` TEXT, `_id` INTEGER PRIMARY KEY AUTOINCREMENT);
                    INSERT INTO main_table_new(date,datetime,log_time,reg_no,airplane_type,day_night,ifr_vfr,flight_type,description) SELECT date,datetime,log_time,reg_no,airplane_type,day_night,ifr_vfr,flight_type,description FROM main_table;
                    DROP TABLE main_table;
                    ALTER TABLE 'main_table_new' RENAME TO 'main_table';
                    COMMIT;
                """.trimIndent())
            }
        }
    }
}