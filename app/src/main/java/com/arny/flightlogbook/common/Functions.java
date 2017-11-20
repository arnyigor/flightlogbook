package com.arny.flightlogbook.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.arny.arnylib.utils.BasePermissions;
import com.arny.flightlogbook.R;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Functions {

    private static final String APP_PREFERENCES = "pilotlogbookprefs";
    public static final String EXEL_FILE_NAME = "PilotLogBook.xls";
    private static final String DOCUMENT_SEPARATOR = ":";
    private static final String FOLDER_SEPARATOR = "/";
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int REQUEST_EXTERNAL_STORAGE_XLS = 110;

    public static final int REQUEST_DBX_EXTERNAL_STORAGE = 101;

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkSDRRWPermessions(Context context, Activity activity,int requestCode) {
        try {
            boolean mlolipop = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
            boolean permissionGranded = false;
            if (mlolipop) {
                permissionGranded = verifyStoragePermissions(activity,requestCode);// Do something for lollipop and above versions
            }
            return permissionGranded || !mlolipop;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getResources().getString(R.string.str_storage_permission_denied), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private static boolean verifyStoragePermissions(Activity activity,int requestCode) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    requestCode
            );
        }
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * if milliseconds==0 returned current datetime,
     * if format==null default "dd MMM yyyy HH:mm:ss.sss"
     *
     * @param milliseconds
     * @param format
     * @return String datetime
     */
    public static String getDateTime(long milliseconds, String format) {
        if (milliseconds==-1){
            return "";
        }
        try {
            milliseconds = (milliseconds == 0) ? Calendar.getInstance().getTimeInMillis() : milliseconds;
            format = (format == null) ? "dd MMM yyyy HH:mm:ss.sss" : format;
            return (new SimpleDateFormat(format, Locale.getDefault())).format(new Date(milliseconds));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param date
     * @param format
     * @return String datetime
     */
    public static String getDateTime(Date date, String format) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            long milliseconds = calendar.getTimeInMillis();
            format = (format == null || format.trim().equals("")) ? "dd MMM yyyy HH:mm:ss.sss" : format;
            return (new SimpleDateFormat(format, Locale.getDefault())).format(new Date(milliseconds));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    public static long convertTimeStringToLong(String myTimestamp, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        Date date;
        try {
            date = formatter.parse(myTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        return date.getTime();
    }

    public static int validateInt(String val) {
        try{
            if (val != null) {
                return Integer.parseInt(val);
            }
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public static long validateLong(String val) {
        try{
            if (val != null) {
                return Long.parseLong(val);
            }
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public static void setPrefString(Context context,String key,String value){
        Functions.getPrefs(context).edit().putString(key, value).apply();
    }

    public static String getPrefString(Context context,String key,String defaultVal){
        return getPrefs(context).getString(key, defaultVal);
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

}
