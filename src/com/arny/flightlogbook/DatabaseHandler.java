package com.arny.flightlogbook;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	//Database Version
	private static final int DATABASE_VERSION = 11;
	//Database Name
	private static final String DATABASE_NAME = "PilotDB";
	//Table Name
	private static final String MAIN_TABLE = "main_table";
	private static final String TYPE_TABLE = "type_table";
	//Column Name
	static final String COLUMN_ID = "_id";
	static final String COLUMN_TYPE_ID = "type_id";
	static final String COLUMN_DATETIME = "datetime";
	static final String COLUMN_STR_TIME = "str_time";
	static final String COLUMN_DATE = "date";
	static final String COLUMN_LOG_TIME = "log_time";
	static final String COLUMN_REG_NO = "reg_no";
	static final String COLUMN_AIRPLANE_TYPE = "airplane_type";
	//static final String COLUMN_AIRPLANE_TYPE_TITLE = "airplane_type_title";
	static final String COLUMN_DAY_NIGHT = "day_night";
	static final String COLUMN_IFR_VFR = "ifr_vfr";
	static final String COLUMN_FLIGHT_TYPE = "flight_type";
	static final String COLUMN_DESCRIPTION = "description";
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
		String CREATE_MAIN_TABLE ="create table " + MAIN_TABLE
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
		String CREATE_TYPE_TABLE ="create table " + TYPE_TABLE
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
		if (oldVersion == 10) {
			/*db.execSQL("DROP TABLE IF EXISTS " + MAIN_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + TYPE_TABLE);
			onCreate(db);*/
		}
	}

	//Insert Value
	public long addFlight(long datetime,int logtime,String reg_no,int airplanetypeid, int daynight, int ifr_vfr,int flighttype,String description) {
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
			Log.i(TAG, "addFlight"+"\n"
					+"datetime = "+datetime+"\n"
					+"logtime = "+logtime+"\n"
					+"reg_no = "+reg_no+"\n"
					+"airplanetypeid = "+airplanetypeid+"\n"
					+"daynight = "+daynight+"\n"
					+"ifr_vfr = "+ifr_vfr+"\n"
					+"flighttype = "+flighttype+"\n"
					+"description = "+description
			);
		return db.insert(MAIN_TABLE, null, values);
	}

	//Insert Value
	public long addType(String airplane_type_title) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_AIRPLANE_TYPE, airplane_type_title);
		long typeid = db.insert(TYPE_TABLE, null, values);
		Log.i(TAG, "addType"+"\n"
				+"airplane_type_title = "+airplane_type_title+"\n"
		);
		return typeid;
	}

	// update Item
	public boolean updateFlight(long datetime,int logtime,String reg_no,int airplanetypeid, int daynight, int ifr_vfr,int flighttype,String description,int keyId) {
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
		Log.i(TAG, "updateFlight"+"\n"
				+"keyId = "+keyId+"\n"
				+"datetime = "+datetime+"\n"
				+"logtime = "+logtime+"\n"
				+"reg_no = "+reg_no+"\n"
				+"airplanetypeid = "+airplanetypeid+"\n"
				+"daynight = "+daynight+"\n"
				+"ifr_vfr = "+ifr_vfr+"\n"
				+"flighttype = "+flighttype+"\n"
				+"description = "+description
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
		Log.i(TAG, "updateType"+"\n"
				+"keyId = "+keyId+"\n"
				+"airplane_type_title = "+airplane_type
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
		if(cursor != null && !cursor.isClosed() && cursor.moveToFirst()){
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
		if(cursor != null && !cursor.isClosed() && cursor.moveToFirst()){
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
		if(cursor != null && !cursor.isClosed()){
			count = cursor.getCount();
			cursor.close();
		}
		return count;
	}

	//Delete Query
	public void removeFlight(int id) {
		//Log.i(TAG, "removeFlight " + "DELETE FROM " + MAIN_TABLE + " where " + COLUMN_ID + "= " + id);
		String countQuery = "DELETE FROM " + MAIN_TABLE + " where " + COLUMN_ID + "= " + id ;
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL(countQuery);
	}

	//Delete Query
	public void removeType(int id) {
		//Log.i(TAG, "removeType " + "DELETE FROM " + TYPE_TABLE + " where " + COLUMN_TYPE_ID + "= " + id);
		String countQuery = "DELETE FROM " + TYPE_TABLE + " where " + COLUMN_TYPE_ID + "= " + id ;
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL(countQuery);
	}

	//Delete Query
	public void removeAllFlights() {
		//Log.i(TAG, "removeAllFlights " + "DELETE FROM " + MAIN_TABLE);
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
	public List<DataList> getFlightItem(int iditem){
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
	public List<DataList> getFlightList(){
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
	public List<DataList> getFlightListByDate(){
		//Log.i(TAG, "getFlightList " + "SELECT  * FROM " + MAIN_TABLE + " ORDER BY " + COLUMN_DATETIME);
		String selectQuery = "SELECT  * FROM " + MAIN_TABLE + " ORDER BY " + COLUMN_DATETIME;
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
	public List<DataList> getTypeItem(int iditem){
		//Log.i(TAG, "getTypeItem " + "SELECT  * FROM " + TYPE_TABLE + " where " + COLUMN_TYPE_ID + "= " + iditem);
		String selectQuery = "SELECT  * FROM " + TYPE_TABLE + " where " + COLUMN_TYPE_ID + "= " + iditem;
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
		return itemList;
	}

	//Get FavList
	public List<DataList> getTypeItemByTitle(String title){
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
	public List<DataList> getTypeList(){
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


