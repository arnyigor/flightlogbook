package com.arny.data.repositories

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.arny.data.db.MainDB

interface BaseDBRepository {
    fun getDb(): MainDB
    fun update(cv: ContentValues, table: String, where: String, whereArgs: Array<Any>, closeDb: Boolean = true): Boolean {
        val db = getDb()
        val supportSQLiteDatabase = db.openHelper.writableDatabase
        db.beginTransaction()
        var update = false
        try {
             update = supportSQLiteDatabase.update(table, SQLiteDatabase.CONFLICT_REPLACE, cv, where, whereArgs)>0
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            if (closeDb) {
                supportSQLiteDatabase.close()
            }
        }
        return update
    }

    fun runSQl(sql: String, closeDb: Boolean = true): Boolean {
        val db = getDb()
        val supportSQLiteDatabase = db.openHelper.writableDatabase
        db.beginTransaction()
        var success = false
        try {
            supportSQLiteDatabase.execSQL(sql)
            db.setTransactionSuccessful()
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            if (closeDb) {
                supportSQLiteDatabase.close()
            }
        }
        return success
    }
}
