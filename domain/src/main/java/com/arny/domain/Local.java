package com.arny.domain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.arny.constants.CONSTS;
import com.arny.data.BuildConfig;
import com.arny.domain.models.Flight;
import com.arny.domain.models.PlaneType;
import com.arny.domain.models.Statistic;
import com.arny.helpers.utils.BasePermissions;
import com.arny.helpers.utils.DBProvider;
import com.arny.helpers.utils.DateTimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.arny.constants.CONSTS.DB.COLUMN_AIRPLANE_TYPE;
import static com.arny.constants.CONSTS.DB.COLUMN_DATETIME;
import static com.arny.constants.CONSTS.DB.COLUMN_DAY_NIGHT;
import static com.arny.constants.CONSTS.DB.COLUMN_DESCRIPTION;
import static com.arny.constants.CONSTS.DB.COLUMN_FLIGHT_TYPE;
import static com.arny.constants.CONSTS.DB.COLUMN_ID;
import static com.arny.constants.CONSTS.DB.COLUMN_IFR_VFR;
import static com.arny.constants.CONSTS.DB.COLUMN_LOG_TIME;
import static com.arny.constants.CONSTS.DB.COLUMN_REG_NO;
import static com.arny.constants.CONSTS.DB.COLUMN_TYPE_ID;
import static com.arny.constants.CONSTS.DB.MAIN_TABLE;
import static com.arny.constants.CONSTS.DB.TYPE_TABLE;
public class Local {


    public static PlaneType getTypeItem(long iditem, Context context) {
	    Cursor cursor = DBProvider.selectDB(TYPE_TABLE, null, COLUMN_TYPE_ID + "=?",new String[]{String.valueOf(iditem)},null, context);
        return new PlaneType();
    }

    //Insert Value
    public static long addType(String planeTypeName, Context context) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_AIRPLANE_TYPE, planeTypeName);
        Log.d(Local.class.getSimpleName(), "addType" + "\n" + "planeTypeName = " + planeTypeName + "\n");
        return DBProvider.insertDB(TYPE_TABLE, values, context);
    }

    // updateReplace Item
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
                + "aircraft_id = " + airplanetypeid + "\n"
                + "daynight = " + daynight + "\n"
                + "ifr_vfr = " + ifr_vfr + "\n"
                + "flighttype = " + flighttype + "\n"
                + "description = " + description);
        return DBProvider.updateDB(MAIN_TABLE, values, "_id=?", new String[]{String.valueOf(keyId)}, context)>0;
    }

    // updateReplace Item
    public static void updateType(String airplane_type, int keyId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE_ID, keyId);
        values.put(COLUMN_AIRPLANE_TYPE, airplane_type);
        if (BuildConfig.DEBUG) Log.d(Local.class.getSimpleName(), "updatePlaneTypeTitle" + "\n"
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

	public static int getFlightsTotal(Context context) {
		String countQuery = "SELECT COUNT(*) FROM main_table" ;
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
	            cnt += DBProvider.getCursorInt(cursor, "cnt");
	            totalByMonth += DBProvider.getCursorInt(cursor, "total_month");
	            listitem.setDt(DBProvider.getCursorLong(cursor,"dt"));
	            listitem.setCnt(DBProvider.getCursorInt(cursor, "cnt"));
	            listitem.setStrMoths(DateTimeUtils.getDateTime(DBProvider.getCursorLong(cursor, "dt"), "MMM yyyy"));
	            listitem.setTotalByMonth(DBProvider.getCursorInt(cursor, "total_month"));
	            listitem.setStrTotalByMonths(DateTimeUtils.strLogTime(DBProvider.getCursorInt(cursor, "total_month")));
	            listitem.setDaysTime(DBProvider.getCursorInt(cursor, "daystime"));
	            daysTime += DBProvider.getCursorInt(cursor, "daystime");
	            listitem.setNightsTime(DBProvider.getCursorInt(cursor, "nighttime"));
	            nightTime += DBProvider.getCursorInt(cursor, "nighttime");
	            listitem.setDnTime(DateTimeUtils.strLogTime(DBProvider.getCursorInt(cursor, "daystime")) + "\n" + DateTimeUtils.strLogTime(DBProvider.getCursorInt(cursor, "nighttime")));
	            listitem.setVfrTime(DBProvider.getCursorInt(cursor, "vfrtime"));
	            vfrtime += DBProvider.getCursorInt(cursor, "vfrtime");
	            ifrtime += DBProvider.getCursorInt(cursor, "ifrtime");
	            listitem.setIfrTime(DBProvider.getCursorInt(cursor, "ifrtime"));
	            listitem.setIvTime(DateTimeUtils.strLogTime(DBProvider.getCursorInt(cursor, "vfrtime")) + "\n" + DateTimeUtils.strLogTime(DBProvider.getCursorInt(cursor, "ifrtime")));
	            circletime += DBProvider.getCursorInt(cursor, "circletime");
	            listitem.setCzmTime(DateTimeUtils.strLogTime(DBProvider.getCursorInt(cursor, "circletime")) + "\n" + DateTimeUtils.strLogTime(DBProvider.getCursorInt(cursor, "zonetime"))+ "\n" + DateTimeUtils.strLogTime(DBProvider.getCursorInt(cursor, "marshtime")));
	            zonetime += DBProvider.getCursorInt(cursor, "zonetime");
	            marshtime += DBProvider.getCursorInt(cursor, "marshtime");

                if (cursor.isFirst()) {
	                firstMonth = DateTimeUtils.getDateTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("dt"))), "MMM yyyy");
                }
                if (cursor.isLast()) {
	                lastMonth = DateTimeUtils.getDateTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("dt"))), "MMM yyyy");
                }
                list.add(listitem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Statistic listitem = new Statistic();
        listitem.setCnt(cnt);
        listitem.setTotalByMonth(totalByMonth);
        listitem.setStrTotalByMonths(DateTimeUtils.strLogTime(totalByMonth));
        listitem.setStrMoths(firstMonth + "\n" + lastMonth);
        listitem.setDnTime(DateTimeUtils.strLogTime(daysTime) + "\n" + DateTimeUtils.strLogTime(nightTime));
        listitem.setIvTime(DateTimeUtils.strLogTime(vfrtime) + "\n" + DateTimeUtils.strLogTime(ifrtime));
        listitem.setCzmTime(DateTimeUtils.strLogTime(circletime) + "\n" + DateTimeUtils.strLogTime(zonetime) + "\n" + DateTimeUtils.strLogTime(marshtime));
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
    public static List<Flight> getFlightListByDate(Context context) {
        return getFlightListByDate(context, null);
    }

    //Get FavList
    public static List<Flight> getFlightListByDate(Context context, String orderBy) {
//        Cursor cursor = DBProvider.selectDB(MAIN_TABLE,null,null,null, orderBy, context);
	    Cursor cursor = DBProvider.queryDB("SELECT _id,date,datetime,log_time,str_time,reg_no,day_night,ifr_vfr,flight_type,description,main_table.airplane_type as airplane_type,type_table.airplane_type as airplane_type_title FROM main_table LEFT JOIN type_table ON type_table.type_id=main_table.airplane_type ORDER BY " + orderBy,null,context);
        ArrayList<Flight> cursorObjectList = new ArrayList<>();
//        for (Flight flight : cursorObjectList) {
//            AircraftType typeItem = getTypeItem(flight.getAircraft_id(), context);
//            if (typeItem != null) {
//                flight.setAirplanetypetitle(typeItem.getTypeName());
//            }
//        }
        return cursorObjectList;
    }


    //Get FavList
    public static PlaneType getType(String title, Context context) {
        Cursor cursor = DBProvider.selectDB(TYPE_TABLE, null, COLUMN_AIRPLANE_TYPE + " = ?", new String[]{title},null, context);
        return new PlaneType();
    }

    //Get FavList
    public static List<PlaneType> getTypeList(Context context) {
        String selectQuery = "SELECT type_id,airplane_type FROM " + TYPE_TABLE + " ORDER BY " + COLUMN_TYPE_ID;
        Cursor cursor = DBProvider.queryDB(selectQuery, null, context);
        return new ArrayList<>();
    }

    public static boolean isAppFileExist(Context context) {
        if (!BasePermissions.isStoragePermissonGranted(context)) {
            Toast.makeText(context, R.string.storage_not_avalable, Toast.LENGTH_LONG).show();
            return false;
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.arny.flightlogbook/files", CONSTS.FILES.EXEL_FILE_NAME);
        return file.exists() && file.isFile();
    }
}
