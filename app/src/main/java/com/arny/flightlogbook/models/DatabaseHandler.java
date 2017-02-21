package com.arny.flightlogbook.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    //Column Name
    public static final String COLUMN_ID = "_id";
    private static final String COLUMN_TYPE_ID = "type_id";
    private static final String COLUMN_DATETIME = "datetime";
    private static final String COLUMN_STR_TIME = "str_time";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_LOG_TIME = "log_time";
    private static final String COLUMN_REG_NO = "reg_no";
    private static final String COLUMN_AIRPLANE_TYPE = "airplane_type";
    private static final String COLUMN_DAY_NIGHT = "day_night";
    private static final String COLUMN_IFR_VFR = "ifr_vfr";
    private static final String COLUMN_FLIGHT_TYPE = "flight_type";
    private static final String COLUMN_DESCRIPTION = "description";
    //Database Version
    private static final int DATABASE_VERSION = 12;
    //Database Name
    private static final String DATABASE_NAME = "PilotDB";
    //Table Name
    private static final String MAIN_TABLE = "main_table";
    private static final String TYPE_TABLE = "type_table";
    private static final String TAG = "LOG_TAG";


	/*Удалить колонку нельзя,можно только добавить.
    Переименовать можно только таблицу
    Поэтому делаем промежуточную таблицу и пересоздаем главную таблицу с новой структурой,затем заносим туда данные из промежуточной таблицы
    */

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //Log.i(TAG, "DatabaseHandler context "+context+" DATABASE_NAME = " + DATABASE_NAME + " DATABASE_VERSION = "+DATABASE_VERSION);
    }

    //Create Table
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Log.i(TAG, "onCreate db");
        String CREATE_MAIN_TABLE = "create table " + MAIN_TABLE
                + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_DATETIME + " TIMESTAMP,"
                + COLUMN_LOG_TIME + " INTEGER,"
                + COLUMN_STR_TIME + " TEXT,"
                + COLUMN_REG_NO + " TEXT,"
                + COLUMN_AIRPLANE_TYPE + " INTEGER,"
                + COLUMN_DAY_NIGHT + " INTEGER,"
                + COLUMN_IFR_VFR + " INTEGER,"
                + COLUMN_FLIGHT_TYPE + " INTEGER,"
                + COLUMN_DESCRIPTION + " TEXT"
                + ")";
        String CREATE_TYPE_TABLE = "create table " + TYPE_TABLE
                + " ("
                + COLUMN_TYPE_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_AIRPLANE_TYPE + " TEXT"
                + ")";
        db.execSQL(CREATE_MAIN_TABLE);
        db.execSQL(CREATE_TYPE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Log.i(TAG, "onUpgrade SQLiteDatabase from " + oldVersion + " to "+newVersion + " version");
        if (oldVersion < 10) {
            db.execSQL("DROP TABLE IF EXISTS " + MAIN_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TYPE_TABLE);
            onCreate(db);
        }
    }

    //Insert Value
    public long addFlight(long datetime, int logtime, String reg_no, int airplanetypeid, int daynight, int ifr_vfr, int flighttype, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATETIME, datetime);
        values.put(COLUMN_LOG_TIME, logtime);
        values.put(COLUMN_REG_NO, reg_no);
        values.put(COLUMN_AIRPLANE_TYPE, airplanetypeid);
        values.put(COLUMN_DAY_NIGHT, daynight);
        values.put(COLUMN_IFR_VFR, ifr_vfr);
        values.put(COLUMN_FLIGHT_TYPE, flighttype);
        values.put(COLUMN_DESCRIPTION, description);
        Log.i(TAG, "addFlight" + "\n"
                + "datetime = " + datetime + "\n"
                + "logtime = " + logtime + "\n"
                + "reg_no = " + reg_no + "\n"
                + "airplanetypeid = " + airplanetypeid + "\n"
                + "daynight = " + daynight + "\n"
                + "ifr_vfr = " + ifr_vfr + "\n"
                + "flighttype = " + flighttype + "\n"
                + "description = " + description
        );
        return db.insert(MAIN_TABLE, null, values);
    }

    //Insert Value
    public long addType(String airplane_type_title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AIRPLANE_TYPE, airplane_type_title);
        long typeid = db.insert(TYPE_TABLE, null, values);
        Log.i(TAG, "addType" + "\n"
                + "airplane_type_title = " + airplane_type_title + "\n"
        );
        return typeid;
    }

    // update Item
    public boolean updateFlight(long datetime, int logtime, String reg_no, int airplanetypeid, int daynight, int ifr_vfr, int flighttype, String description, int keyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, keyId);
        values.put(COLUMN_DATETIME, datetime);
        values.put(COLUMN_LOG_TIME, logtime);
        values.put(COLUMN_REG_NO, reg_no);
        values.put(COLUMN_AIRPLANE_TYPE, airplanetypeid);
        values.put(COLUMN_DAY_NIGHT, daynight);
        values.put(COLUMN_IFR_VFR, ifr_vfr);
        values.put(COLUMN_FLIGHT_TYPE, flighttype);
        values.put(COLUMN_DESCRIPTION, description);
        Log.i(TAG, "updateFlight" + "\n"
                + "keyId = " + keyId + "\n"
                + "datetime = " + datetime + "\n"
                + "logtime = " + logtime + "\n"
                + "reg_no = " + reg_no + "\n"
                + "airplanetypeid = " + airplanetypeid + "\n"
                + "daynight = " + daynight + "\n"
                + "ifr_vfr = " + ifr_vfr + "\n"
                + "flighttype = " + flighttype + "\n"
                + "description = " + description
        );
        return db.update(MAIN_TABLE, values, COLUMN_ID + "=" + keyId, null) > 0;
    }

    // update Item
    public boolean updateType(String airplane_type, int keyId) {
        //Log.i(TAG, "updateType ");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE_ID, keyId);
        values.put(COLUMN_AIRPLANE_TYPE, airplane_type);
        Log.i(TAG, "updateType" + "\n"
                + "keyId = " + keyId + "\n"
                + "airplane_type_title = " + airplane_type
        );
        return db.update(TYPE_TABLE, values, COLUMN_TYPE_ID + "=" + keyId, null) > 0;
    }

    //Get COLUMN Count
    public int getFlightCount() {
        ////Log.i(TAG, "getFlightCount ");
        String countQuery = "SELECT  * FROM " + MAIN_TABLE;
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    //Get COLUMN Count
    public int getFlightsTime() {
        //Log.i(TAG, "getFlightsTime ");
        String countQuery = "SELECT SUM(" + COLUMN_LOG_TIME + ") FROM " + MAIN_TABLE;
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
            //Log.i(TAG, "getFlightsTime cursor getInt " + cursor.getInt(0));
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    //Get COLUMN Count
    public int getTypeCount() {
        ////Log.i(TAG, "getTypeCount ");
        String countQuery = "SELECT  * FROM " + TYPE_TABLE;
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && !cursor.isClosed()) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    public List<Statistic> getStatistic(String whereQuery) {
        ////Log.i(TAG, "getTypeCount ");
        String statisticQuery = "SELECT " + COLUMN_DATETIME + " as dt, " +
                "   COUNT(*) as cnt, " +
                "  (SELECT SUM(" + COLUMN_LOG_TIME + ") FROM main_table WHERE strftime('%m',datetime(outer_data." + COLUMN_DATETIME + "/1000, 'unixepoch')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch'))) AS total_month, " +
                "  (SELECT SUM(" + COLUMN_LOG_TIME + ") FROM main_table WHERE strftime('%m',datetime(outer_data." + COLUMN_DATETIME + "/1000, 'unixepoch')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch')) AND " + COLUMN_DAY_NIGHT + " = 0) AS daystime, " +
                "  (SELECT SUM(" + COLUMN_LOG_TIME + ") FROM main_table WHERE strftime('%m',datetime(outer_data." + COLUMN_DATETIME + "/1000, 'unixepoch')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch')) AND " + COLUMN_DAY_NIGHT + " = 1) AS nighttime, " +
                "  (SELECT SUM(" + COLUMN_LOG_TIME + ") FROM main_table WHERE strftime('%m',datetime(outer_data." + COLUMN_DATETIME + "/1000, 'unixepoch')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch')) AND " + COLUMN_IFR_VFR + " = 0) AS vfrtime, " +
                "  (SELECT SUM(" + COLUMN_LOG_TIME + ") FROM main_table WHERE strftime('%m',datetime(outer_data." + COLUMN_DATETIME + "/1000, 'unixepoch')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch')) AND " + COLUMN_IFR_VFR + " = 1) AS ifrtime, " +
                "  (SELECT SUM(" + COLUMN_LOG_TIME + ") FROM main_table WHERE strftime('%m',datetime(outer_data." + COLUMN_DATETIME + "/1000, 'unixepoch')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch')) AND " + COLUMN_FLIGHT_TYPE + " = 0) AS circletime, " +
                "  (SELECT SUM(" + COLUMN_LOG_TIME + ") FROM main_table WHERE strftime('%m',datetime(outer_data." + COLUMN_DATETIME + "/1000, 'unixepoch')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch')) AND " + COLUMN_FLIGHT_TYPE + " = 1) AS zonetime, " +
                "  (SELECT SUM(" + COLUMN_LOG_TIME + ") FROM main_table WHERE strftime('%m',datetime(outer_data." + COLUMN_DATETIME + "/1000, 'unixepoch')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch')) AND " + COLUMN_FLIGHT_TYPE + " = 2) AS marshtime " +
                " FROM main_table AS outer_data" +
                whereQuery +
                " GROUP BY strftime('%m',datetime(" + COLUMN_DATETIME + "/1000, 'unixepoch'))" +
                " ORDER BY dt";
//        Log.i(TAG, "getStatistic statisticQuery = " + statisticQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(statisticQuery, null);
        List<Statistic> list = new ArrayList<>();
        String firstMonth = "", lastMonth = "";
        int totalByMonth = 0, cnt = 0, daysTime = 0, nightTime = 0, vfrtime = 0, ifrtime = 0, circletime = 0, zonetime = 0, marshtime = 0;

        if (cursor.moveToFirst()) {
            do {
                Statistic listitem = new Statistic();
                cnt += Functions.validateInt(cursor.getString(cursor.getColumnIndex("cnt")));
                totalByMonth += Functions.validateInt(cursor.getString(cursor.getColumnIndex("total_month")));
                listitem.setDT(Functions.validateLong(cursor.getString(cursor.getColumnIndex("dt"))));
                listitem.setCnt(Functions.validateInt(cursor.getString(cursor.getColumnIndex("cnt"))));
                listitem.setStrMoths(Functions.getDateTime(Functions.validateLong(cursor.getString(cursor.getColumnIndex("dt"))),"MMM yyyy"));
                listitem.setTotalByMonth(Functions.validateInt(cursor.getString(cursor.getColumnIndex("total_month"))));
                listitem.setStrTotalByMonths(Functions.strLogTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("total_month")))));
                listitem.setDaysTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("daystime"))));
                daysTime += Functions.validateInt(cursor.getString(cursor.getColumnIndex("daystime")));
                listitem.setNightsTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("nighttime"))));
                nightTime += Functions.validateInt(cursor.getString(cursor.getColumnIndex("nighttime")));
                listitem.setDnTime(Functions.strLogTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("daystime")))) + "\n" + Functions.strLogTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("nighttime")))));

                listitem.setVfrTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("vfrtime"))));

                vfrtime += Functions.validateInt(cursor.getString(cursor.getColumnIndex("vfrtime")));
                ifrtime += Functions.validateInt(cursor.getString(cursor.getColumnIndex("ifrtime")));

                listitem.setIfrTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("ifrtime"))));
                listitem.setIVTime(Functions.strLogTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("vfrtime")))) + "\n" + Functions.strLogTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("ifrtime")))));

                circletime += Functions.validateInt(cursor.getString(cursor.getColumnIndex("circletime")));
                listitem.setCzmTime(Functions.strLogTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("circletime")))) + "\n" + Functions.strLogTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("zonetime"))))+ "\n" + Functions.strLogTime(Functions.validateInt(cursor.getString(cursor.getColumnIndex("marshtime")))));
                zonetime += Functions.validateInt(cursor.getString(cursor.getColumnIndex("zonetime")));
                marshtime += Functions.validateInt(cursor.getString(cursor.getColumnIndex("marshtime")));

//                Log.i(TAG, "getStatistic dt = " + cursor.getString(cursor.getColumnIndex("dt")));
//                Log.i(TAG, "getStatistic cnt = " + cursor.getString(cursor.getColumnIndex("cnt")));
//                Log.i(TAG, "getStatistic total_month = " + cursor.getString(cursor.getColumnIndex("total_month")));
//                Log.i(TAG, "getStatistic daystime = " + cursor.getString(cursor.getColumnIndex("daystime")));
//                Log.i(TAG, "getStatistic nighttime = " + cursor.getString(cursor.getColumnIndex("nighttime")));
//                Log.i(TAG, "getStatistic vfrtime = " + cursor.getString(cursor.getColumnIndex("vfrtime")));
//                Log.i(TAG, "getStatistic ifrtime = " + cursor.getString(cursor.getColumnIndex("ifrtime")));
//                Log.i(TAG, "getStatistic circletime = " + cursor.getString(cursor.getColumnIndex("circletime")));
//                Log.i(TAG, "getStatistic zonetime = " + cursor.getString(cursor.getColumnIndex("zonetime")));
//                Log.i(TAG, "getStatistic marshtime = " + cursor.getString(cursor.getColumnIndex("marshtime")));

                if (cursor.isFirst()) {
                    firstMonth = Functions.getDateTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("dt"))),"MMM yyyy");
                }
                if (cursor.isLast()) {
                    lastMonth = Functions.getDateTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("dt"))),"MMM yyyy");
                }
                list.add(listitem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Statistic listitem = new Statistic();
        listitem.setCnt(cnt);
        listitem.setTotalByMonth(totalByMonth);
        listitem.setStrTotalByMonths(Functions.strLogTime(totalByMonth));
        listitem.setStrMoths(firstMonth + "\n" + lastMonth);
        listitem.setDnTime(Functions.strLogTime(daysTime) + "\n" + Functions.strLogTime(nightTime));
        listitem.setIVTime(Functions.strLogTime(vfrtime) + "\n" + Functions.strLogTime(ifrtime));
        listitem.setCzmTime(Functions.strLogTime(circletime) + "\n" + Functions.strLogTime(zonetime) + "\n" + Functions.strLogTime(marshtime));
        listitem.setVfrTime(vfrtime);
        listitem.setVfrTime(ifrtime);
        listitem.setCircleTime(circletime);
        listitem.setZoneTime(zonetime);
        listitem.setMarshTime(marshtime);
        list.add(listitem);
        return list;
    }

    //Delete Query
    public void removeFlight(int id) {
        //Log.i(TAG, "removeFlight " + "DELETE FROM " + MAIN_TABLE + " where " + COLUMN_ID + "= " + id);
        String countQuery = "DELETE FROM " + MAIN_TABLE + " where " + COLUMN_ID + "= " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(countQuery);
    }

    //Delete Query
    public void removeType(int id) {
        Log.i(TAG, "removeType " + "DELETE FROM " + TYPE_TABLE + " where " + COLUMN_TYPE_ID + "= " + id);
        String countQuery = "DELETE FROM " + TYPE_TABLE + " where " + COLUMN_TYPE_ID + "= " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(countQuery);
    }

    //Delete Query
    public void removeAllFlights() {
        Log.i(TAG, "removeAllFlights " + "DELETE FROM " + MAIN_TABLE);
        String countQuery = "DELETE FROM " + MAIN_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(countQuery);
    }

    //Delete Query
    public void removeAllTypes() {
        //Log.i(TAG, "removeAllTypes " + "DELETE FROM " + TYPE_TABLE);
        String countQuery = "DELETE FROM " + TYPE_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(countQuery);
    }

    //Get FavList
    public List<DataList> getFlightItem(int iditem) {
        //Log.i(TAG, "getFlightItem " + "SELECT  * FROM " + MAIN_TABLE + " where " + COLUMN_ID + "= " + iditem);
        String selectQuery = "SELECT  * FROM " + MAIN_TABLE + " where " + COLUMN_ID + "= " + iditem;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<DataList> itemList = new ArrayList<DataList>();
        if (cursor.moveToFirst()) {
            do {
                DataList list = new DataList();
                list.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_ID))));
                list.setDatetime(Long.parseLong(cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME))));
                list.setLogtime(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_LOG_TIME))));
                list.setReg_no(cursor.getString(cursor.getColumnIndex(COLUMN_REG_NO)));
                list.setAirplanetypeid(cursor.getInt(cursor.getColumnIndex(COLUMN_AIRPLANE_TYPE)));
                list.setDaynight(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_DAY_NIGHT))));
                list.setIfrvfr(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_IFR_VFR))));
                list.setFlighttype(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_FLIGHT_TYPE))));
                list.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                itemList.add(list);
            } while (cursor.moveToNext());
        }
        return itemList;
    }

    //Get FavList
    public List<DataList> getFlightList() {
        //Log.i(TAG, "getFlightList " + "SELECT  * FROM " + MAIN_TABLE);
        String selectQuery = "SELECT  * FROM " + MAIN_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        //Log.i(TAG, "getFlightList cursor = " + cursor);
        List<DataList> allList = new ArrayList<DataList>();
        if (cursor.moveToFirst()) {
            do {
                DataList list = new DataList();
                list.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_ID))));
                list.setDatetime(Long.parseLong(cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME))));
                list.setLogtime(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_LOG_TIME))));
                list.setReg_no(cursor.getString(cursor.getColumnIndex(COLUMN_REG_NO)));
                list.setAirplanetypeid(cursor.getInt(cursor.getColumnIndex(COLUMN_AIRPLANE_TYPE)));
                list.setDaynight(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_DAY_NIGHT))));
                list.setIfrvfr(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_IFR_VFR))));
                list.setFlighttype(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_FLIGHT_TYPE))));
                list.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                allList.add(list);
            } while (cursor.moveToNext());
        }
        return allList;
    }

    //Get FavList
    public List<DataList> getFlightListByDate() {
        String selectQuery = "SELECT  * FROM " + MAIN_TABLE + " ORDER BY " + COLUMN_DATETIME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<DataList> allList = new ArrayList<DataList>();
        if (cursor.moveToFirst()) {
            do {
                DataList list = new DataList();
                list.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_ID))));
                list.setDatetime(Long.parseLong(cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME))));
                list.setLogtime(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_LOG_TIME))));
                list.setReg_no(cursor.getString(cursor.getColumnIndex(COLUMN_REG_NO)));
                list.setAirplanetypeid(cursor.getInt(cursor.getColumnIndex(COLUMN_AIRPLANE_TYPE)));
                list.setDaynight(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_DAY_NIGHT))));
                list.setIfrvfr(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_IFR_VFR))));
                list.setFlighttype(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_FLIGHT_TYPE))));
                list.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                allList.add(list);
            } while (cursor.moveToNext());
        }
        return allList;
    }

    //Get FavList
    public List<DataList> getFlightListFilter(long dateTimeFrom, long dateTimeTo, String filterQuery) {

        String query = "SELECT  * FROM " + MAIN_TABLE;
        if ((dateTimeFrom != 0) && (dateTimeTo != 0)) {
            query += " WHERE " + COLUMN_DATETIME + ">=" + dateTimeFrom + " AND " + COLUMN_DATETIME + "<=" + dateTimeTo;
            ;
        } else if (dateTimeFrom != 0) {
            query += " WHERE " + COLUMN_DATETIME + ">=" + dateTimeFrom;
        } else if (dateTimeTo != 0) {
            query += " WHERE " + COLUMN_DATETIME + "<=" + dateTimeTo;
        }
        if (filterQuery != null) {
            query += " AND " + filterQuery;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<DataList> allList = new ArrayList<DataList>();
        if (cursor.moveToFirst()) {
            do {
                DataList list = new DataList();
                list.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_ID))));
                list.setDatetime(Long.parseLong(cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME))));
                list.setLogtime(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_LOG_TIME))));
                list.setReg_no(cursor.getString(cursor.getColumnIndex(COLUMN_REG_NO)));
                list.setAirplanetypeid(cursor.getInt(cursor.getColumnIndex(COLUMN_AIRPLANE_TYPE)));
                list.setDaynight(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_DAY_NIGHT))));
                list.setIfrvfr(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_IFR_VFR))));
                list.setFlighttype(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_FLIGHT_TYPE))));
                list.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                allList.add(list);
            } while (cursor.moveToNext());
        }
        return allList;
    }

    //Get FavList
    public List<DataList> getFlightListByPlaineType(String mPlanetype) {
        String selectQuery = "SELECT MAIN.* FROM " + MAIN_TABLE + " as MAIN "
                + " JOIN " + TYPE_TABLE + " as TYPE"
                + " ON " + "MAIN." + COLUMN_AIRPLANE_TYPE + " = TYPE." + COLUMN_TYPE_ID
                + " WHERE TYPE." + COLUMN_AIRPLANE_TYPE + " LIKE '%" + mPlanetype + "%'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<DataList> allList = new ArrayList<DataList>();
        if (cursor.moveToFirst()) {
            do {
                DataList list = new DataList();
                list.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_ID))));
                list.setDatetime(Long.parseLong(cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME))));
                list.setLogtime(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_LOG_TIME))));
                list.setReg_no(cursor.getString(cursor.getColumnIndex(COLUMN_REG_NO)));
                list.setAirplanetypeid(cursor.getInt(cursor.getColumnIndex(COLUMN_AIRPLANE_TYPE)));
                list.setDaynight(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_DAY_NIGHT))));
                list.setIfrvfr(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_IFR_VFR))));
                list.setFlighttype(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_FLIGHT_TYPE))));
                list.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                allList.add(list);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return allList;
    }

    //Get FavList
    public List<DataList> getTypeItem(int iditem) {
        //Log.i(TAG, "getTypeItem " + "SELECT  * FROM " + TYPE_TABLE + " where " + COLUMN_TYPE_ID + "= " + iditem);
        String selectQuery = "SELECT * FROM " + TYPE_TABLE + " where " + COLUMN_TYPE_ID + "= " + iditem;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<DataList> itemList = new ArrayList<DataList>();
        if (cursor.moveToFirst()) {
            do {
                DataList list = new DataList();
                list.setAirplanetypeid(cursor.getInt(0));
                list.setAirplanetypetitle(cursor.getString(1));
                itemList.add(list);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemList;
    }

    //Get FavList
    public List<DataList> getTypeItemByTitle(String title) {
        //Log.i(TAG, "getTypeItemByTitle " + "SELECT  * FROM " + TYPE_TABLE + " where " + COLUMN_AIRPLANE_TYPE + "= " + title);
        String selectQuery = "SELECT  * FROM " + TYPE_TABLE + " where " + COLUMN_AIRPLANE_TYPE + "= " + title;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<DataList> itemList = new ArrayList<DataList>();
        if (cursor.moveToFirst()) {
            do {
                DataList list = new DataList();
                list.setAirplanetypeid(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_ID))));
                list.setAirplanetypetitle(cursor.getString(cursor.getColumnIndex(COLUMN_AIRPLANE_TYPE)));
                itemList.add(list);
            } while (cursor.moveToNext());
        }
        return itemList;
    }

    //Get FavList
    public List<DataList> getTypeList() {
        //Log.i(TAG, "getTypeList " + "SELECT  * FROM " + TYPE_TABLE);
        String selectQuery = "SELECT  * FROM " + TYPE_TABLE + " ORDER BY " + COLUMN_TYPE_ID;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<DataList> allList = new ArrayList<DataList>();
        if (cursor.moveToFirst()) {
            do {
                DataList list = new DataList();
                list.setAirplanetypeid(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_ID))));
                list.setAirplanetypetitle(cursor.getString(cursor.getColumnIndex(COLUMN_AIRPLANE_TYPE)));
                allList.add(list);
            } while (cursor.moveToNext());
        }
        return allList;
    }

}


