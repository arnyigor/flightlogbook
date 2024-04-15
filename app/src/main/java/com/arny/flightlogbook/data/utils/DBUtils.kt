package com.arny.flightlogbook.data.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.util.Log
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arny.core.utils.*
import com.arny.flightlogbook.presentation.utils.Utility
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.ArrayList
import kotlin.Comparator

class DBUtils {
    companion object {
        private fun getSortedRoomMigrations(filenames: ArrayList<String>): ArrayList<String> {
            val list = ArrayList<String>()
            for (filename in filenames) {
                val match = getRoomMigrationMatch(filename)
                if (!match.empty()) {
                    var start = 0
                    var end = 0
                    try {
                        start = getRoomMigrationVersion(filename, 0)
                        end = getRoomMigrationVersion(filename, 1)
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                    if (start != end && end != 0 && start != 0) {
                        list.add(filename)
                    }
                }
            }
            list.sortWith(Comparator { o1, o2 ->
                var start1 = 0
                var end1 = 0
                var start2 = 0
                var end2 = 0
                try {
                    val match1 = getRoomMigrationMatch(o1)
                    val match2 = getRoomMigrationMatch(o2)
                    if (!match1.empty() && !match2.empty()) {
                        start1 = getRoomMigrationVersion(o1, 0)
                        end1 = getRoomMigrationVersion(o1, 1)
                        start2 = getRoomMigrationVersion(o2, 0)
                        end2 = getRoomMigrationVersion(o2, 1)
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }

                if (start1 == start2 && end1 == end2) {
                    return@Comparator 0
                }
                val compareStart = Integer.compare(start1, start2)
                if (compareStart != 0) {
                    return@Comparator compareStart
                }
                val compareEnd = Integer.compare(end1, end2)
                if (compareEnd != 0) {
                    return@Comparator compareEnd
                }
                0
            })
            return list
        }

        /**
         * Версия миграции
         *
         * @param filename имя файла
         * @param position 0 - start|1 - finish
         * @return номер версии
         */
        fun getRoomMigrationVersion(filename: String, position: Int): Int {
            return Integer.parseInt(getRoomMigrationMatch(filename)!!.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[position])
        }

        /**
         * Нахождение версий миграций библиотеки Room
         *
         * @param filename
         * @return
         */
        fun getRoomMigrationMatch(filename: String): String? {
            return Utility.match(filename, "^room_{1}(\\d+_{1}\\d+)_{1}.*\\.sql", 1)
        }

        private fun addRoomMigration(context: Context, startVersion: Int, endVersion: Int, migrationsFile: String): Migration {
            return object : Migration(startVersion, endVersion) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    runRoomMigrations(context, database, migrationsFile)
                }
            }
        }

        fun getRoomMigrations(context: Context): Array<Migration?> {
            val migrationsFiles = getSortedRoomMigrations(FileUtils.listAssetFiles(context, "migrations"))
            val migrationArrayList = ArrayList<Migration>()
            for (migrationsFile in migrationsFiles) {
                val start = getRoomMigrationVersion(migrationsFile, 0)
                val end = getRoomMigrationVersion(migrationsFile, 1)
                migrationArrayList.add(addRoomMigration(context, start, end, migrationsFile))
            }
            val migrations = arrayOfNulls<Migration>(migrationArrayList.size)
            for (i in migrationArrayList.indices) {
                migrations[i] = migrationArrayList[i]
                val migration = migrationArrayList[i]
                Log.i(DBUtils::class.java.simpleName, "getRoomMigrations:startVersion:" +
                        migration.startVersion + " endVersion:" + migration.endVersion)
            }
            return migrations
        }

        private fun runRoomMigrations(context: Context, database: SupportSQLiteDatabase, migrationsFile: String) {
            val cursorMigrations = database.query("SELECT filename FROM migrations WHERE filename='$migrationsFile'")
            if (cursorMigrations == null || cursorMigrations.count == 0) {
                val sql = Utility.readAssetFile(context, "migrations", migrationsFile)
                val migrationVersion = getRoomMigrationVersion(migrationsFile, 0)
                val version = database.version
                runMigration(database, sql)
            }
        }

          fun runMigration(database: SupportSQLiteDatabase, sql: String?) {
            if (sql != null) {
                val sqls = sql.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                database.beginTransaction()
                try {
                    for (s in sqls) {
                        if (!s.empty()) {
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

        fun runRoomMigrations(context: Context, database: SupportSQLiteDatabase) {
            val migrationsFiles = getSortedRoomMigrations(FileUtils.listAssetFiles(context, "migrations"))
            for (migrationsFile in migrationsFiles) {
                val cursorMigrations = database.query("SELECT filename FROM migrations WHERE filename='$migrationsFile'")
                if (cursorMigrations == null || cursorMigrations.count == 0) {
                    val sql = Utility.readAssetFile(context, "migrations", migrationsFile)
                    val migrationVersion = getRoomMigrationVersion(migrationsFile, 0)
                    val version = database.version
                    Log.d(DBUtils::class.java.simpleName, "runRoomMigrations: migrationsFile:$migrationsFile version:$version end:$migrationVersion")
                    runMigration(database, sql)
                }
            }
        }

        @Throws(Exception::class)
        fun importDB(packageName: String, dbName: String) {
            val sd = Environment.getExternalStorageDirectory()
            val data = Environment.getDataDirectory()
            if (sd.canWrite()) {
                val currentDBPath = ("//data//" + packageName
                        + "//databases//" + dbName)
                val backupDBPath = "<backup db filename>" // From SD directory.
                val backupDB = File(data, currentDBPath)
                val currentDB = File(sd, backupDBPath)
                val src = FileInputStream(backupDB).channel
                val dst = FileOutputStream(currentDB).channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()
            }
        }

        @Throws(Exception::class)
        fun exportDB() {
            val sd = Environment.getExternalStorageDirectory()
            val data = Environment.getDataDirectory()
            if (sd.canWrite()) {
                val currentDBPath = ("//data//" + "<package name>"
                        + "//databases//" + "<db name>")
                val backupDBPath = "<destination>"
                val currentDB = File(data, currentDBPath)
                val backupDB = File(sd, backupDBPath)
                val src = FileInputStream(currentDB).channel
                val dst = FileOutputStream(backupDB).channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()
            }

        }

        fun update(
                db: RoomDatabase,
                cv: ContentValues,
                table: String,
                where: String,
                whereArgs: Array<Any>,
                closeDb: Boolean
        ): Boolean {
            val supportSQLiteDatabase = db.openHelper.writableDatabase
            var update = false
            db.runInTransaction {
                update = supportSQLiteDatabase.update(table, SQLiteDatabase.CONFLICT_REPLACE, cv, where, whereArgs) > 0
            }
            if (closeDb) {
                supportSQLiteDatabase.close()
            }
            return update
        }

        fun runSQl(db: RoomDatabase, sql: String, closeDb: Boolean): Boolean {
            val supportSQLiteDatabase = db.openHelper.writableDatabase
            var success = false
            db.runInTransaction {
                supportSQLiteDatabase.execSQL(sql)
                success = true
            }
            if (closeDb) {
                supportSQLiteDatabase.close()
            }
            return success
        }

        fun queryItem(db: RoomDatabase, sql: String, function: (Cursor) -> Unit) {
            queryDb(db, sql).toItem(function)
        }

        fun queryList(db: RoomDatabase, sql: String, function: (c: Cursor) -> Unit) {
            queryDb(db, sql).toList(function)

        }

        private fun queryDb(db: RoomDatabase, sql: String): Cursor? {
            return db.openHelper
                    .writableDatabase
                    .query(sql)
        }
    }
}