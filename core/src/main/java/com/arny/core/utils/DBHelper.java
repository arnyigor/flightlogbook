package com.arny.core.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

class DBHelper extends SQLiteOpenHelper {
    static int dbVersion;
    static String dbName;
    private Context context;
	private static volatile DBHelper mInstance;
    private static final Object LOCK = new Object();

	public static DBHelper getInstance(Context ctx) {
		DBHelper localInstance = mInstance;
		if (localInstance == null) {
			synchronized (LOCK) {
				localInstance = mInstance;
				if (localInstance == null) {
					mInstance = localInstance = new DBHelper(ctx.getApplicationContext());
				}
			}
		}
		return localInstance;
	}


	private DBHelper(Context context) {
        super(context, dbName, null, dbVersion);
        this.context = context;
    }

	@Override
    public void onCreate(SQLiteDatabase db) {
        setTableMigrations(db);
        dbMigrations(db, context);
    }
	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dbMigrations(db, context);
    }
	/**
     * Создание таблицы миграций
     * @param db
     */
    private void setTableMigrations(SQLiteDatabase db) {
        clearDb(db);
        db.execSQL("CREATE TABLE migrations ( "
                + "_id INTEGER PRIMARY KEY NOT NULL,"
                + "filename TEXT(40) NOT NULL,"
                + "applytime TEXT(20) NOT NULL"
                + ");");
    }
	/**
     * Миграции базы данных
     * @param db
     * @param context
     */
    private void dbMigrations(SQLiteDatabase db, Context context) {
        ArrayList<String> filenames = FileUtils.listAssetFiles(context,"migrations");
        filenames = sortFilenames(filenames);
        if (!isTableExists(db, "migrations")){
            setTableMigrations(db);
        }
        filenames = newMigrations(db, filenames);
        runMigration(db, filenames, context);
    }
	/**
     * Проверяем наличие таблицы
     * @param db
     * @param tableName
     * @return
     */
    private boolean isTableExists(SQLiteDatabase db,String tableName) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"' ",null);
        if(cursor!=null && cursor.getCount()>0) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }
	private void clearDb(SQLiteDatabase db){
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master", null);
            if (cursor != null && cursor.getCount()>0){
                int cnt = cursor.getCount();
                cursor.moveToFirst();
                String tableName = cursor.getString(0);
            }
            db.setTransactionSuccessful();
            if (cursor != null) {
                cursor.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
	/**
     * Исключение миграций которые уже есть в таблице
     * @param db
     * @param filenames
     * @return
     */
    private ArrayList<String> newMigrations(SQLiteDatabase db, ArrayList<String> filenames) {
        ArrayList<String> toDel = new ArrayList<>();
        for (String fName : filenames) {
            String name = Utility.removeExtension(fName);
            db.beginTransaction();
            try {
                Cursor cursor = db.rawQuery("SELECT * FROM migrations WHERE filename = '"+name+"'", null);
                if (cursor != null && cursor.getCount()>0){
                    cursor.moveToFirst();
                    String dbName = "";//DBProvider.getCursorString(cursor, "filename");
                    if (!Utility.empty(dbName)){
                        toDel.add(dbName+".sql");
                    }
                }
                db.setTransactionSuccessful();
                if (cursor != null) {
                    cursor.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
        for (String del : toDel) {
            filenames.remove(del);
        }
        return filenames;
    }
	/**
     * Запуск миграций из файлов
     * @param db
     * @param filenames
     * @param context
     */
    private void runMigration(SQLiteDatabase db, ArrayList<String> filenames, Context context){
        if (!filenames.isEmpty()){
            for (String fn:filenames) {
                String sql = Utility.readAssetFile(context,"migrations",fn);
                if (!Utility.empty(sql)) {
                    db.beginTransaction();
                    try {
                        String[] splittedSqls = sql.split("\\;");
                        for (String splittedSql : splittedSqls) {
                            db.execSQL(splittedSql.trim());
                        }
                        ContentValues values = new ContentValues();
                        String patternapp = "^([app|lib]{1,}\\_\\d{8}[_]{0,}.*)\\.sql";
                        String fnam = Utility.match(fn, patternapp,1);
                        values.put("filename", fnam);
                        values.put("applytime", DateTimeUtils.getDateTime(System.currentTimeMillis(),"yyyyMMdd"));
                        db.insert("migrations", null, values);
                        db.setTransactionSuccessful();
                    }catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        db.endTransaction();
                    }
                }
            }
        }
    }
	/**
     * Сортировка файлов по датам и префиксам,lib,потом app,
     * @param files
     * @return ArrayList<String>
     */
    private static ArrayList<String> sortFilenames(ArrayList<String> files){
        ArrayList<String> apps = new ArrayList<>();
        ArrayList<String> appsinfo = new ArrayList<>();
        ArrayList<String> libs = new ArrayList<>();
        ArrayList<String> libsinfo = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();
        String patternapp = "^app_(\\d{8})([_]{0,}.*)\\.sql";
        String patternlib = "^lib_(\\d{8})([_]{0,}.*)\\.sql";
        for (String fileName : files) {
            String appdate = Utility.match(fileName, patternapp,1);
            if (appdate!=null) {
                String appinfo = Utility.match(fileName, patternapp,2);
                apps.add(appdate);
                appinfo = appinfo == null ? "" : appinfo;
                appsinfo.add(appinfo);
            }
            String libdate = Utility.match(fileName, patternlib,1);
            if (libdate!=null) {
                String libinfo = Utility.match(fileName, patternlib,2);
                libs.add(libdate);
                libinfo = libinfo == null ? "" : libinfo;
                libsinfo.add(libinfo);
            }
        }
        apps = Utility.sortDates(apps,"yyyyMMdd");
        libs = Utility.sortDates(libs,"yyyyMMdd");
        apps = recreateFilesName(apps,"app_",appsinfo,".sql");
        libs = recreateFilesName(libs,"lib_",libsinfo,".sql");
        for (String lib : libs) {
            result.add(lib);
        }
        for (String app : apps) {
            result.add(app);
        }
        apps.clear();
        libs.clear();
        appsinfo.clear();
        libsinfo.clear();
        return result;
    }

    /**
     * Восстанавливаем имя файла
     * @param dates
     * @param prefix
     * @param infos
     * @param extention
     * @return
     */
    private static ArrayList<String> recreateFilesName(ArrayList<String> dates, String prefix,ArrayList<String> infos, String extention){
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            result.add(prefix + dates.get(i) + infos.get(i) + extention);
        }
        return result;
    }
}