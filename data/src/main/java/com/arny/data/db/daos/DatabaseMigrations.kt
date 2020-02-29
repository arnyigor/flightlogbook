package com.arny.data.db.daos

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseMigrations {
    companion object{
        val MIGRATION_15_16: Migration = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE main_table ADD COLUMN ground_time INTEGER")
                database.execSQL("ALTER TABLE main_table ADD COLUMN night_time INTEGER")
            }
        }
    }
}