package com.arny.flightlogbook.models;

import android.os.Environment;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

public class Functions{

	public static boolean matcher(String preg,String string) {
	    return Pattern.matches(preg, string);
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
        String format = "dd.mm.YYYY";
        for (HashMap.Entry<String, String> entry : pregs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (Pattern.matches(key, myTimestamp)) {
                format = value;
                break;
            }
        }
        return format;
    }

    /**
     * if milliseconds==0 returned current datetime,
     * if format==null default "dd MMM yyyy HH:mm:ss.sss"
     * @param milliseconds
     * @param format
     * @return String datetime
     */
    public static String getDateTime(long milliseconds, String format) {
        milliseconds = (milliseconds == 0) ? Calendar.getInstance().getTimeInMillis() : milliseconds;
        format = (format == null) ? "dd MMM yyyy HH:mm:ss.sss" : format;
        return (new SimpleDateFormat(format, Locale.getDefault())).format(new Date(milliseconds));
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
        if (val != null) {
            return Integer.parseInt(val);
        }
        return 0;
    }

    public static long validateLong(String val) {
        if (val != null) {
            return Long.parseLong(val);
        }
        return 0;
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
        int hours = 0;
        int mins = 0;
        String delimeter = (time.contains(":"))? ":":".";
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
