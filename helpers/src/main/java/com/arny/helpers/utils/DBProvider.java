package com.arny.helpers.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class DBProvider {

    public static long insertDB(String table, ContentValues contentValues, Context context) {
        Log.d(DBProvider.class.getSimpleName(), "insertDB: table:" + table + " contentValues:" + contentValues);
        long rowID = 0;
        SQLiteDatabase db = connectDB(context);
        db.beginTransaction();
        rowID = db.insert(table, null, contentValues);
        db.setTransactionSuccessful();
        db.endTransaction();
        return rowID;
    }

    public static int insertReplaceDB(Context context, String table, String where, String[] args, ContentValues cv) {
        Log.d(DBProvider.class.getSimpleName(), "insertReplaceDB: table:" + table + " ContentValues:" + cv + " where:" + where + " args:" + Arrays.toString(args));
        Cursor cursor = selectDB(table, null, where, args, null, context);
        if (cursor != null && cursor.moveToFirst()) {
            return updateDB(table, cv, where, args, context);
        } else {
            return (int) insertDB(table, cv, context);
        }
    }

    public static long insertOrUpdateDB(Context context, String table, ContentValues contentValues) {
        Log.d(DBProvider.class.getSimpleName(), "insertOrUpdateDB: table:" + table + " contentValues:" + contentValues);
        long rowID = 0;
        SQLiteDatabase db = connectDB(context);
        db.beginTransaction();
        rowID = db.insertWithOnConflict(table, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        db.setTransactionSuccessful();
        db.endTransaction();
        return rowID;
    }

    public static Cursor selectDB(String table, String[] columns, String where, String orderBy, Context context) {
        Log.d(DBProvider.class.getSimpleName(), "selectDB: table:" + table + " columns:" + Arrays.toString(columns) + " where:" + where + " orderBy:" + orderBy);
        return connectDB(context).query(table, columns, where, null, null, null, orderBy);
    }

    public static Cursor selectDB(String table, String[] columns, String where, String[] whereArgs, String orderBy, Context context) {
        Log.d(DBProvider.class.getSimpleName(), "selectDB: table:" + table + " columns:" + Arrays.toString(columns) + " where:" + where + " whereArgs:" + Arrays.toString(whereArgs) + " orderBy:" + orderBy);
        return connectDB(context).query(table, columns, where, whereArgs, null, null, orderBy);
    }

    public static Cursor queryDB(String sqlQuery, String[] selectionArgs, Context context) {
        Log.d(DBProvider.class.getSimpleName(), "queryDB: query:" + sqlQuery + " selectionArgs:" + Arrays.toString(selectionArgs));
        return connectDB(context).rawQuery(sqlQuery, selectionArgs);
    }

    public static int deleteDB(String table, String where, Context context) {
        Log.d(DBProvider.class.getSimpleName(), "deleteDB: table:" + table + " where:" + where);
        int rowCount;
        SQLiteDatabase db = connectDB(context);
        db.beginTransaction();
        rowCount = db.delete(table, where, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        return rowCount;
    }

    public static int deleteDB(String table, String where, String[] whereArgs, Context context) {
        Log.d(DBProvider.class.getSimpleName(), "deleteDB: table:" + table + " where:" + where + "=whereArgs:" + Arrays.toString(whereArgs));
        int rowCount = 0;
        SQLiteDatabase db = connectDB(context);
        db.beginTransaction();
        rowCount = db.delete(table, where, whereArgs);
        db.setTransactionSuccessful();
        db.endTransaction();
        return rowCount;
    }

    public static int updateDB(String table, ContentValues contentValues, String where, Context context) {
        Log.d(DBProvider.class.getSimpleName(), "updateDB: table:" + table + " contentValues:" + contentValues + " where:" + where);
        int rowCount = 0;
        SQLiteDatabase db = connectDB(context);
        db.beginTransaction();
        rowCount = db.update(table, contentValues, where, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        return rowCount;
    }

    public static int updateDB(String table, ContentValues contentValues, String where, String[] whereArgs, Context context) {
        Log.d(DBProvider.class.getSimpleName(), "updateDB: table:" + table + " contentValues:" + contentValues + " where:" + where + " whereArgs:" + Arrays.toString(whereArgs));
        int rowCount = 0;
        SQLiteDatabase db = connectDB(context);
        db.beginTransaction();
        rowCount = db.update(table, contentValues, where, whereArgs);
        db.setTransactionSuccessful();
        db.endTransaction();
        return rowCount;
    }

    public static void initDB(Context context, String name, int version) {
        DBHelper.dbName = name;
        DBHelper.dbVersion = version;
        SQLiteDatabase db = connectDB(context);
        db.getVersion();
    }

    private static SQLiteDatabase connectDB(Context context) {
        return DBHelper.getInstance(context).getWritableDatabase();
    }

    @NonNull
    public static String getSqlTable(Class<?> aClass) {
        return aClass.getSimpleName().toLowerCase();
    }

}
