package com.arny.flightlogbook.models;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.arny.flightlogbook.BuildConfig;
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
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int REQUEST_EXTERNAL_STORAGE_XLS = 110;

    public static final int REQUEST_DBX_EXTERNAL_STORAGE = 101;

    public static boolean matcher(String preg, String string) {
        return Pattern.matches(preg, string);
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
        pregs.put("^[0-9]{1,2}\\-\\W+\\-[0-9]{2}$", "dd-MMM-yy");
        pregs.put("^[0-9]{1,2}\\-\\W+\\-[0-9]{4}$", "dd-MMM-yyyy");
        pregs.put("^[0-9]{1,2}\\s\\W+\\s[0-9]{2}$", "dd MMM yy");
        pregs.put("^[0-9]{1,2}\\s[0-9]{2}\\s[0-9]{2}$", "dd MM yy");
        pregs.put("^[0-9]{1,2}\\s[0-9]{2}\\s[0-9]{4}$", "dd MM yyyy");
        String format = "dd.mm.yyyy";
        for (HashMap.Entry<String, String> entry : pregs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (BuildConfig.DEBUG){
                Log.i(Functions.class.getSimpleName(), "dateFormatChooser: myTimestamp = " + myTimestamp);
                Log.i(Functions.class.getSimpleName(), "dateFormatChooser: key = " + key);
            }
            if (match(key, myTimestamp,0) !=null) {
                format = match(key, myTimestamp,0);
                break;
            }
        }
        if (BuildConfig.DEBUG){
            Log.i(Functions.class.getSimpleName(), "dateFormatChooser: format = " + format);
        }
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
        Calendar mCalendar = Calendar.getInstance();
        SimpleDateFormat curFormater = new SimpleDateFormat(format, Locale.getDefault());
        Date dateObj = null;
        try {
            dateObj = curFormater.parse(myTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mCalendar.setTime(dateObj);
        return mCalendar.getTimeInMillis();
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
        return pad(h) + ":" + pad(m);
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
        String delimeter = (time.contains(":")) ? ":" : ".";
        int posDelim = time.indexOf(delimeter);
        try {
            hours = Integer.parseInt(time.substring(0, posDelim));
            mins = Integer.parseInt(time.substring(posDelim + 1, time.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mins + (hours * 60);
    }

}
