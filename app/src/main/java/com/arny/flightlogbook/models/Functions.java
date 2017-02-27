package com.arny.flightlogbook.models;

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
import android.util.Log;
import android.widget.Toast;

import com.arny.flightlogbook.BuildConfig;
import com.arny.flightlogbook.R;

import java.io.File;
import java.io.InputStream;
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

    public static boolean matcher(String preg, String string) {
        return Pattern.matches(preg, string);
    }

    public static String getSDFilePath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(DOCUMENT_SEPARATOR);
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + FOLDER_SEPARATOR + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(DOCUMENT_SEPARATOR);
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        else if (isKitKat && !DocumentsContract.isDocumentUri(context, uri)){
            boolean document = false;
            String folders = "", type = "",external;
            for (String segm : uri.getPathSegments()) {
                if (segm.contains(DOCUMENT_SEPARATOR)) {
                    type = segm.split(DOCUMENT_SEPARATOR)[0];
                    folders = segm.split(DOCUMENT_SEPARATOR)[1];
                    document = true;
                    break;
                }
            }
            if (type.equals("primary")) {
                external = Environment.getExternalStorageDirectory().getPath();
            }else {
                external = "/storage/" + type;
            }
            if (document){
                return external + FOLDER_SEPARATOR + folders + FOLDER_SEPARATOR + uri.getLastPathSegment();
            }else{
                return uri.getPath();
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String match(String where,String pattern,int groupnum){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(where);
        while(m.find()) {
            if (!m.group(groupnum).equals("")) {
                return m.group(groupnum);
            }
        }
        return null;
    }

    public static String dateFormatChooser(String myTimestamp) {
        HashMap<String, String> pregs = new HashMap<>();
        pregs.put("^[0-9]{1,2}\\.[0-9]{2}\\.[0-9]{4}$", "dd.MM.yyyy");
        pregs.put("^[0-9]{1,2}\\.[0-9]{2}\\.[0-9]{2}$", "dd.MM.yy");
        pregs.put("^[0-9]{1,2}\\-\\.*\\-[0-9]{2}$", "dd-MMM-yy");
        pregs.put("^[0-9]{1,2}\\-.*\\-[0-9]{4}$", "dd-MMM-yyyy");
        pregs.put("^[0-9]{1,2}\\s\\.*\\s[0-9]{2}$", "dd MMM yy");
        pregs.put("^[0-9]{1,2}\\s\\.*\\s[0-9]{4}$", "dd MMM yyyy");
        pregs.put("^[0-9]{1,2}\\s[0-9]{2}\\s[0-9]{2}$", "dd MM yy");
        pregs.put("^[0-9]{1,2}\\s[0-9]{2}\\s[0-9]{4}$", "dd MM yyyy");
        String format = "dd MMM yyyy";
        for (HashMap.Entry<String, String> entry : pregs.entrySet()) {
            if (Pattern.matches(entry.getKey(), myTimestamp)) {
                format = entry.getValue();
                break;
            }
        }
        Log.i(Functions.class.getSimpleName(), "dateFormatChooser: format = " + format);
        return format;
    }

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
        Log.i(Functions.class.getSimpleName(), "convertTimeStringToLong: myTimestamp = " + myTimestamp);
        Log.i(Functions.class.getSimpleName(), "convertTimeStringToLong: format = " + format);
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

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static boolean isFileExist(Context context) {
        if (!Functions.isExternalStorageAvailable() || Functions.isExternalStorageReadOnly()) {
            Toast.makeText(context, R.string.storage_not_avalable, Toast.LENGTH_LONG).show();
            return false;
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.arny.flightlogbook/files", EXEL_FILE_NAME);
        return file.exists() && file.isFile();
    }

    public static String strLogTime(int logtime) {
        int h = logtime / 60;
        int m = logtime % 60;
        return pad(h) + DOCUMENT_SEPARATOR + pad(m);
    }

    public static String pad(int c) {
        if (c >= 10) {
            return String.valueOf(c);
        } else {
            return "0" + String.valueOf(c);
        }
    }

    public static long randLong(long min, long max) {
        Random rnd = new Random();
        if (min > max) {
            throw new IllegalArgumentException("min>max");
        }
        if (min == max) {
            return min;
        }
        long n = rnd.nextLong();
        n = n == Long.MIN_VALUE ? 0 : n < 0 ? -n : n;
        n = n % (max - min);
        return min + n;
    }

    public static int randInt(int min, int max) {
        Random rnd = new Random();
        int range = max - min + 1;
        return rnd.nextInt(range) + min;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    public static int convertStringToTime(String time) {
        Log.i(Functions.class.getSimpleName(), "convertStringToTime: time = " + time);
        int hours = 0;
        int mins = 0;
        String delimeter = (time.contains(DOCUMENT_SEPARATOR)) ? DOCUMENT_SEPARATOR : ".";
        int posDelim = time.indexOf(delimeter);
        try {
            hours = Integer.parseInt(time.substring(0, posDelim));
            mins = Integer.parseInt(time.substring(posDelim + 1, time.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mins + (hours * 60);
    }

    public static boolean empty( final String s ) {
        return s == null || s.trim().isEmpty();
    }



}
