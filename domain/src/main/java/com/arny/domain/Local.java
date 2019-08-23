package com.arny.domain;

import android.content.Context;
import android.database.Cursor;

import com.arny.domain.models.Flight;
import com.arny.domain.models.Statistic;
import com.arny.helpers.utils.DBProvider;
import com.arny.helpers.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;
public class Local {


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

    //Get FavList
    public static List<Flight> getFlightListByDate(Context context, String orderBy) {
//        Cursor cursor = DBProvider.selectDB(MAIN_TABLE,null,null,null, orderBy, context);
	    Cursor cursor = DBProvider.queryDB("SELECT _id,date,datetime,log_time,str_time,reg_no,day_night,ifr_vfr,flight_type,description,main_table.airplane_type as airplane_type,type_table.airplane_type as airplane_type_title FROM main_table LEFT JOIN type_table ON type_table.type_id=main_table.airplane_type ORDER BY " + orderBy,null,context);
        ArrayList<Flight> cursorObjectList = new ArrayList<>();
//        for (Flight flight : cursorObjectList) {
//            AircraftType typeItem = getTypeItem(flight.getPlaneId(), context);
//            if (typeItem != null) {
//                flight.setAirplanetypetitle(typeItem.getTypeName());
//            }
//        }
        return cursorObjectList;
    }

}
