package com.arny.flightlogbook.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.arny.arnylib.database.DBProvider;
import com.arny.flightlogbook.BuildConfig;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.*;

import java.util.ArrayList;
import java.util.List;
public class Local {
    //Column Name
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TYPE_ID = "type_id";
    public static final String COLUMN_DATETIME = "datetime";
    public static final String COLUMN_LOG_TIME = "log_time";
    public static final String COLUMN_REG_NO = "reg_no";
    public static final String COLUMN_AIRPLANE_TYPE = "airplane_type";
    public static final String COLUMN_DAY_NIGHT = "day_night";
    public static final String COLUMN_IFR_VFR = "ifr_vfr";
    public static final String COLUMN_FLIGHT_TYPE = "flight_type";
    public static final String COLUMN_DESCRIPTION = "description";
    //Database Version
    public static final int DATABASE_VERSION = 12;
    //Database Name
    public static final String DATABASE_NAME = "PilotDB";
    //Table Name
    public static final String MAIN_TABLE = "main_table";
    public static final String TYPE_TABLE = "type_table";

    //Insert Value
    public static long addFlight(long datetime, int logtime, String reg_no, int airplanetypeid, int daynight, int ifr_vfr, int flighttype, String description, Context context) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATETIME, datetime);
        values.put(COLUMN_LOG_TIME, logtime);
        values.put(COLUMN_REG_NO, reg_no);
        values.put(COLUMN_AIRPLANE_TYPE, airplanetypeid);
        values.put(COLUMN_DAY_NIGHT, daynight);
        values.put(COLUMN_IFR_VFR, ifr_vfr);
        values.put(COLUMN_FLIGHT_TYPE, flighttype);
        values.put(COLUMN_DESCRIPTION, description);
        if (BuildConfig.DEBUG){
            Log.d(Local.class.getSimpleName(), "addFlight" + "\n"
                    + "datetime = " + datetime + "\n"
                    + "logtime = " + logtime + "\n"
                    + "reg_no = " + reg_no + "\n"
                    + "airplanetypeid = " + airplanetypeid + "\n"
                    + "daynight = " + daynight + "\n"
                    + "ifr_vfr = " + ifr_vfr + "\n"
                    + "flighttype = " + flighttype + "\n"
                    + "description = " + description);
        }
        return DBProvider.insertDB(MAIN_TABLE, values, context);
    }

    public static String getPlaneType(int airplane_type_id, Context context) {
        Type type = getTypeItem(airplane_type_id, context);
        if (type != null) {
            return type.getTypeName();
        }
        return context.getResources().getText(R.string.no_type).toString();
    }

    public static Type getTypeItem(int iditem, Context context) {
	    Cursor cursor = DBProvider.selectDB(TYPE_TABLE, null, COLUMN_TYPE_ID + "=?",new String[]{String.valueOf(iditem)},null, context);
        return DBProvider.getCursorObject(cursor, Type.class);
    }

    //Insert Value
    public static long addType(String planeTypeName, Context context) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_AIRPLANE_TYPE, planeTypeName);
        Log.d(Local.class.getSimpleName(), "addType" + "\n"
                    + "planeTypeName = " + planeTypeName + "\n");
        return DBProvider.insertDB(TYPE_TABLE, values, context);
    }

    // update Item
    public static boolean updateFlight(long datetime, int logtime, String reg_no, int airplanetypeid, int daynight, int ifr_vfr, int flighttype, String description, int keyId, Context context) {
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
        Log.d(Local.class.getSimpleName(), "updateFlight" + "\n"
                + "keyId = " + keyId + "\n"
                + "datetime = " + datetime + "\n"
                + "logtime = " + logtime + "\n"
                + "reg_no = " + reg_no + "\n"
                + "airplanetypeid = " + airplanetypeid + "\n"
                + "daynight = " + daynight + "\n"
                + "ifr_vfr = " + ifr_vfr + "\n"
                + "flighttype = " + flighttype + "\n"
                + "description = " + description);
        return DBProvider.updateDB(MAIN_TABLE, values, "_id=?", new String[]{String.valueOf(keyId)}, context)>0;
    }

    // update Item
    public static void updateType(String airplane_type, int keyId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE_ID, keyId);
        values.put(COLUMN_AIRPLANE_TYPE, airplane_type);
        if (BuildConfig.DEBUG) Log.d(Local.class.getSimpleName(), "updateType" + "\n"
                + "keyId = " + keyId + "\n"
                + "airplane_type_title = " + airplane_type);

        DBProvider.updateDB(TYPE_TABLE, values, COLUMN_TYPE_ID + "=?",new String[]{String.valueOf(keyId)}, null);
    }

    //Get COLUMN Count
    public static int getFlightsTime(Context context) {
        String countQuery = "SELECT SUM(log_time) FROM main_table" ;
        int count = 0;
        Cursor cursor = DBProvider.queryDB(countQuery,null, context);
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public static List<Statistic> getStatistic(String whereQuery, Context context) {
        String statisticQuery = "SELECT  datetime  as dt, " +
                "   COUNT(*) as cnt, " +
                "  (SELECT SUM(log_time) FROM main_table WHERE strftime('%m',datetime(outer_data.datetime/1000, 'unixepoch', 'localtime')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch', 'localtime'))) AS total_month, " +
                "  (SELECT SUM(log_time) FROM main_table WHERE strftime('%m',datetime(outer_data.datetime/1000, 'unixepoch', 'localtime')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch', 'localtime')) AND day_night = 0) AS daystime, " +
                "  (SELECT SUM(log_time) FROM main_table WHERE strftime('%m',datetime(outer_data.datetime/1000, 'unixepoch', 'localtime')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch')) AND day_night = 1) AS nighttime, " +
                "  (SELECT SUM(log_time) FROM main_table WHERE strftime('%m',datetime(outer_data.datetime/1000, 'unixepoch', 'localtime')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch', 'localtime')) AND ifr_vfr = 0) AS vfrtime, " +
                "  (SELECT SUM(log_time) FROM main_table WHERE strftime('%m',datetime(outer_data.datetime/1000, 'unixepoch', 'localtime')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch', 'localtime')) AND ifr_vfr = 1) AS ifrtime, " +
                "  (SELECT SUM(log_time) FROM main_table WHERE strftime('%m',datetime(outer_data.datetime/1000, 'unixepoch', 'localtime')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch', 'localtime')) AND flight_type = 0) AS circletime, " +
                "  (SELECT SUM(log_time) FROM main_table WHERE strftime('%m',datetime(outer_data.datetime/1000, 'unixepoch', 'localtime')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch', 'localtime')) AND flight_type = 1) AS zonetime, " +
                "  (SELECT SUM(log_time) FROM main_table WHERE strftime('%m',datetime(outer_data.datetime/1000, 'unixepoch', 'localtime')) = strftime('%m',datetime(main_table.datetime/1000, 'unixepoch', 'localtime')) AND flight_type = 2) AS marshtime " +
                " FROM main_table AS outer_data " +
                whereQuery +
                " GROUP BY strftime('%m',datetime(datetime/1000, 'unixepoch', 'localtime'))" +
                " ORDER BY dt";
        Cursor cursor = DBProvider.queryDB(statisticQuery,null, context);
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
    public static void removeFlight(int id, Context context) {
        DBProvider.deleteDB(MAIN_TABLE, "_id = ?", new String[]{String.valueOf(id)}, context);
    }

    //Delete Query
    public static void removeType(int id, Context context) {
        DBProvider.deleteDB(TYPE_TABLE, "type_id = ?", new String[]{String.valueOf(id)}, context);
    }

    //Delete Query
    public static void removeAllFlights(Context context) {
        DBProvider.deleteDB(MAIN_TABLE,null, context);
    }

    //Delete Query
    public static void removeAllTypes(Context context) {
        DBProvider.deleteDB(TYPE_TABLE, null, context);
    }

    //Get COLUMN Count
    public static int getTypeCount(Context context) {
        int count = 0;
        Cursor cursor = DBProvider.selectDB("type_table",null,null,null, context);
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    //Get FavList
    public static List<Flight> getFlightItem(int id, Context context) {
        Cursor cursor = DBProvider.selectDB(MAIN_TABLE,null,"_id = ?",new String[]{String.valueOf(id)},null, context);
        return DBProvider.getCursorObjectList(cursor, Flight.class);
    }

    //Get FavList
    public static List<Flight> getFlightList(Context context) {
        Cursor cursor = DBProvider.selectDB(MAIN_TABLE,null,null,null, context);
        return DBProvider.getCursorObjectList(cursor, Flight.class);
    }

    //Get FavList
    public static List<Flight> getFlightListByDate(Context context) {
        Cursor cursor = DBProvider.selectDB(MAIN_TABLE,null,null,null,"datetime", context);
        return DBProvider.getCursorObjectList(cursor, Flight.class);
    }

    //Get FavList
    public static List<Flight> getFlightListFilter(long dateTimeFrom, long dateTimeTo, String filterQuery, Context context) {

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
        Cursor cursor = DBProvider.queryDB(query, null, context);
        return DBProvider.getCursorObjectList(cursor, Flight.class);
    }

    //Get FavList
    public static List<Flight> getFlightListByPlaineType(String mPlanetype, Context context) {
        String selectQuery = "SELECT MAIN.* FROM " + MAIN_TABLE + " as MAIN "
                + " JOIN " + TYPE_TABLE + " as TYPE"
                + " ON " + "MAIN." + COLUMN_AIRPLANE_TYPE + " = TYPE." + COLUMN_TYPE_ID
                + " WHERE TYPE." + COLUMN_AIRPLANE_TYPE + " LIKE '%" + mPlanetype + "%'";
        Cursor cursor = DBProvider.queryDB(selectQuery, null, context);
        return DBProvider.getCursorObjectList(cursor, Flight.class);
    }

    //Get FavList
    public static Type getType(String title, Context context) {
        Cursor cursor = DBProvider.selectDB(TYPE_TABLE, null, COLUMN_AIRPLANE_TYPE + " = ?", new String[]{title},null, context);
        return DBProvider.getCursorObject(cursor, Type.class);
    }

    //Get FavList
    public static List<Type> getTypeList(Context context) {
        String selectQuery = "SELECT  * FROM " + TYPE_TABLE + " ORDER BY " + COLUMN_TYPE_ID;
        Cursor cursor = DBProvider.queryDB(selectQuery, null, context);
        return DBProvider.getCursorObjectList(cursor, Type.class);
    }
}
