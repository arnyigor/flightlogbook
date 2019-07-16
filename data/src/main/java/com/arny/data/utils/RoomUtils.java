package com.arny.data.utils;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.arny.helpers.utils.DBProvider;
import com.arny.helpers.utils.FileUtils;
import com.arny.helpers.utils.Stopwatch;
import com.arny.helpers.utils.Utility;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Sedoy on 15.07.2019
 */
public class RoomUtils {

    /**
     * Сортировка файлов миграций Room
     *
     * @param filenames
     * @return
     */
    @NotNull
    private static ArrayList<String> getSortedRoomMigrations(ArrayList<String> filenames) {
        ArrayList<String> list = new ArrayList<>();
        for (String filename : filenames) {
            String match = getRoomMigrationMatch(filename);
            if (!Utility.empty(match)) {
                int start = 0;
                int end = 0;
                try {
                    start = getRoomMigrationVersion(filename, 0);
                    end = getRoomMigrationVersion(filename, 1);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (start != end && end != 0 && start != 0) {
                    list.add(filename);
                }
            }
        }
        Collections.sort(list, (o1, o2) -> {
            int start1 = 0;
            int end1 = 0;
            int start2 = 0;
            int end2 = 0;
            try {
                String match1 = getRoomMigrationMatch(o1);
                String match2 = getRoomMigrationMatch(o2);
                if (!Utility.empty(match1) && !Utility.empty(match2)) {
                    start1 = getRoomMigrationVersion(o1, 0);
                    end1 = getRoomMigrationVersion(o1, 1);
                    start2 = getRoomMigrationVersion(o2, 0);
                    end2 = getRoomMigrationVersion(o2, 1);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (start1 == start2 && end1 == end2) {
                return 0;
            }
            int compareStart = Integer.compare(start1, start2);
            if (compareStart != 0) {
                return compareStart;
            }
            int compareEnd = Integer.compare(end1, end2);
            if (compareEnd != 0) {
                return compareEnd;
            }
            return 0;
        });
        return list;
    }

    /**
     * Версия миграции
     *
     * @param filename имя файла
     * @param position 0 - start|1 - finish
     * @return номер версии
     */
    public static int getRoomMigrationVersion(String filename, int position) {
        return Integer.parseInt(getRoomMigrationMatch(filename).split("_")[position]);
    }

    /**
     * Нахождение версий миграций библиотеки Room
     *
     * @param filename
     * @return
     */
    public static String getRoomMigrationMatch(String filename) {
        return Utility.match(filename, "^room_{1}(\\d+_{1}\\d+)_{1}.*\\.sql", 1);
    }

    @NonNull
    private static Migration addRoomMigration(Context context, int startVersion, int endVersion, String migrationsFile) {
        Log.d(DBProvider.class.getSimpleName(), "addRoomMigration:  startVersion:" + startVersion + " endVersion:" + endVersion);
        return new Migration(startVersion, endVersion) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                runRoomMigrations(context, database, migrationsFile);
            }
        };
    }

    public static Migration[] getRoomMigrations(Context context) {
        ArrayList<String> migrationsFiles = getSortedRoomMigrations(FileUtils.listAssetFiles(context, "migrations"));
        Log.d(DBProvider.class.getSimpleName(), "getRoomMigrations: файлы миграции:" + migrationsFiles);
        ArrayList<Migration> migrationArrayList = new ArrayList<>();
        for (String migrationsFile : migrationsFiles) {
            int start = getRoomMigrationVersion(migrationsFile, 0);
            int end = getRoomMigrationVersion(migrationsFile, 1);
            migrationArrayList.add(addRoomMigration(context, start, end, migrationsFile));
        }
        Migration[] migrations = new Migration[migrationArrayList.size()];
        for (int i = 0; i < migrationArrayList.size(); i++) {
            migrations[i] = migrationArrayList.get(i);
            Migration migration = migrationArrayList.get(i);
            Log.i(DBProvider.class.getSimpleName(), "getRoomMigrations:startVersion:" +
                    migration.startVersion + " endVersion:" + migration.endVersion);
        }
        return migrations;
    }

    private static void runRoomMigrations(Context context, SupportSQLiteDatabase database, String migrationsFile) {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        Cursor cursorMigrations = database.query("SELECT filename FROM migrations WHERE filename='" + migrationsFile + "'");
        if (cursorMigrations == null || cursorMigrations.getCount() == 0) {
            String sql = Utility.readAssetFile(context, "migrations", migrationsFile);
            int migrationVersion = getRoomMigrationVersion(migrationsFile, 0);
            int version = database.getVersion();
            Log.d(DBProvider.class.getSimpleName(), "runRoomMigrations: migrationsFile:" + migrationsFile + " version:" + version + " end:" + migrationVersion);
            runMigration(database, migrationsFile, sql);
        }
        Log.d(DBProvider.class.getSimpleName(), "runRoomMigrations: database ALL execSQL...OK time:" + stopwatch.getElapsedTimeSecs(3));
        stopwatch.stop();
    }

    private static void runMigration(SupportSQLiteDatabase database, String migrationsFile, String sql) {
        if (sql != null) {
            String[] sqls = sql.split(";");
            database.beginTransaction();
            try {
                for (String s : sqls) {
                    if (!Utility.empty(s)) {
                        database.execSQL(s);
                    }
                }
                database.execSQL("INSERT INTO migrations (filename) VALUES ('" + migrationsFile + "');");
                database.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            }
            database.endTransaction();
        }
    }

    public static void runRoomMigrations(Context context, SupportSQLiteDatabase database) {
        ArrayList<String> migrationsFiles = getSortedRoomMigrations(FileUtils.listAssetFiles(context, "migrations"));
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        for (String migrationsFile : migrationsFiles) {
            Cursor cursorMigrations = database.query("SELECT filename FROM migrations WHERE filename='" + migrationsFile + "'");
            if (cursorMigrations == null || cursorMigrations.getCount() == 0) {
                String sql = Utility.readAssetFile(context, "migrations", migrationsFile);
                int migrationVersion = getRoomMigrationVersion(migrationsFile, 0);
                int version = database.getVersion();
                Log.d(DBProvider.class.getSimpleName(), "runRoomMigrations: migrationsFile:" + migrationsFile + " version:" + version + " end:" + migrationVersion);
                runMigration(database, migrationsFile, sql);
            }
        }
        Log.i(DBProvider.class.getSimpleName(), "runRoomMigrations: database ALL execSQL...OK time:" + stopwatch.getElapsedTimeSecs(3));
        stopwatch.stop();
    }

}
