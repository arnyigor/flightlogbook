package com.arny.data.repositories

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.arny.data.db.MainDB

interface BaseDBRepository {
    fun getDb(): MainDB
    fun update(mainDB: MainDB, cv: ContentValues, table: String, where: String, whereArgs: Array<Any>, closeDb: Boolean = true): Int {
        val supportSQLiteDatabase = mainDB.openHelper.writableDatabase
        val update = supportSQLiteDatabase.update(table, SQLiteDatabase.CONFLICT_REPLACE, cv, where, whereArgs)
        if (closeDb) {
            supportSQLiteDatabase.close()
        }
        return update
    }
}
