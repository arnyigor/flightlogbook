package com.arny.helpers.utils;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeUtils {

    public static final String[] RU_MONTHES_FULL_EXT = new String[]{"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};
    public static final String[] RU_MONTHES_FULL = new String[]{"январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"};
    private static final String[] RU_MONTHES_LO = new String[]{"янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек",};
    private static final String[] RU_MONTHES_LO_EXT = new String[]{"янв", "февр", "мар", "апр", "май", "июн", "июл", "авг", "сент", "окт", "нояб", "дек",};
    private static final String[] RU_MONTHES_UP = new String[]{"Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};
    private static final String TIME_SEPARATOR_TWICE_DOT = ":";
    private static final String TIME_SEPARATOR_DOT = ".";

    public static boolean matcher(String regex, String string) {
        return Pattern.matches(regex, string);
    }

    public static String match(String where, String pattern, int groupnum) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(where);
        while (m.find()) {
            if (!m.group(groupnum).equals("")) {
                return m.group(groupnum);
            }
        }
        return null;
    }

    private static Locale getLocale(String myTimestamp) {
        boolean isUS = Pattern.matches("(?i).*[A-z]+.*", myTimestamp);
        return isUS ? Locale.US : Locale.getDefault();
    }

    public static String dateFormatChooser(String myTimestamp) {
        HashMap<String, String> pregs = new HashMap<>();
        pregs.put("^\\d{1,2}\\.\\d{2}\\.\\d{4}$", "dd.MM.yyyy");
        pregs.put("^\\d{1,2}\\.\\d{2}\\.\\d{2}$", "dd.MM.yy");
        pregs.put("^\\d{1,2}\\-\\D+\\-\\d{2}$", "dd-MMM-yy");
        pregs.put("^\\d{1,2}\\-\\D+\\-\\d{4}$", "dd-MMM-yyyy");
        pregs.put("^\\d{1,2}\\s+\\D+\\s\\d{2}$", "dd MMM yy");
        pregs.put("^\\d{1,2}\\s+\\D+\\s+\\d{4}$", "dd MMM yyyy");
        pregs.put("^\\d{1,2}\\s+\\d{2}\\s\\d{2}$", "dd MM yy");
        pregs.put("^\\d{1,2}\\d{2}\\d{4}$", "ddMMyyyy");
        String format = "dd MMM yyyy";
        for (HashMap.Entry<String, String> entry : pregs.entrySet()) {
            boolean matches = Pattern.matches(entry.getKey(), myTimestamp);
            if (matches) {
                String value = entry.getValue();
//                System.out.println("dateFormatChooser myTimestamp:" + myTimestamp + ": format:" + value);
//				Log.d(DateTimeUtils.class.getSimpleName(), "dateFormatChooser myTimestamp:" + myTimestamp + ": format:" + value);
                return value;
            }
        }
//        System.out.println("dateFormatChooser myTimestamp:" + myTimestamp + ": format:" + format);
//		Log.d(DateTimeUtils.class.getSimpleName(), "dateFormatChooser myTimestamp:" + myTimestamp + ": format:" + format);
        return format;
    }

    private static long getEsTime(long startTime, long curTime, int iter, int tot) {
        if (iter == 0) {
            return 0;
        }
        long a = (curTime - startTime) / iter;
        return (a * tot) - (a * iter);
    }

    public static Date getUnixTimeDate(long timeStamp) {
        return new Date(timeStamp * 1000);
    }

    public static long millisecondsToUnixTime(long ms) {
        return ms / 1000;
    }

    public static long unixTimeToMilliseconds(long timestamp) {
        return timestamp * 1000;
    }

    /**
     * Примерное время выполнения
     *
     * @param startTime время старта выполнения итераций  milliseconds
     * @param curTime   текущее время итерации milliseconds
     * @param iter      текущая итерация
     * @param tot       всего итераций
     * @return "min:sec"
     */
    public static String getEstimateTime(long startTime, long curTime, int iter, int tot) {
        long esTime = getEsTime(startTime, curTime, iter, tot);
        int min = (int) (((esTime / 1000) / 60) % 60);
        int sec = (int) ((esTime / 1000) % 60);
        return pad(min) + ":" + pad(sec);
    }

    /**
     * Истекает время
     *
     * @param endTime конечное время milliseconds
     * @param curTime текущее время milliseconds
     * @return "min:sec"
     */
    public static String getEstimateTime(long endTime, long curTime) {
        long dTime = endTime - curTime;
        dTime = dTime <= 0 ? 0 : dTime;
        int min = (int) (((dTime / 1000) / 60) % 60);
        int sec = (int) ((dTime / 1000) % 60);
        return pad(min) + ":" + pad(sec);
    }

    /**
     * Конвертируем секунды в часы:минуты:секунды // TODO: 04.12.2017 нужно расширить
     *
     * @param secs
     * @return
     */
    public static String convertTime(long secs) {
        secs = secs * 1000;
//        long days = TimeUnit.MILLISECONDS.toDays(secs);
//        secs -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(secs);
        secs -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(secs);
        secs -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(secs);
        return String.format("%s:%s:%s", pad((int) hours), pad((int) minutes), pad((int) seconds));
    }

    /**
     * Конвертируем часы,минуты, секунды в секунды
     *
     * @param hours
     * @param mins
     * @param secs
     * @return
     */
    public static long convertTime(int hours, int mins, int secs) {
        return hours * 3600 + mins * 60 + secs;
    }

    public static String getMonthName(Context context, long timestamp, boolean full) {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_NO_YEAR;
        if (!full) {
            flags |= DateUtils.FORMAT_ABBREV_MONTH;
        }
        return DateUtils.formatDateTime(context, timestamp, flags);
    }

    public static String getMonthName(Calendar calendar) {
        return getCalendarFieldName(calendar, Calendar.MONTH, Calendar.LONG);
    }
    public static String getWeekDayName(Calendar calendar) {
        return getCalendarFieldName(calendar, Calendar.DAY_OF_WEEK, Calendar.SHORT);
    }

    public static String getCalendarFieldName(Calendar calendar, int field, int style) {
        return calendar.getDisplayName(field, style, Locale.getDefault());
    }

    public static String getDateTime(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        return dateFormat.format(date);
    }

    private static DateFormatSymbols myDateFormatSymbolsFull = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return RU_MONTHES_FULL;
        }
    };

    private static DateFormatSymbols myDateFormatSymbolsFullEXT = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return RU_MONTHES_FULL_EXT;
        }
    };

    private static DateFormatSymbols myDateFormatSymbols = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return RU_MONTHES_LO;
        }
    };
    private static DateFormatSymbols myDateFormatSymbolsExt = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return RU_MONTHES_LO_EXT;
        }
    };

    private static DateFormatSymbols myDateFormatSymbolsUp = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return RU_MONTHES_UP;
        }
    };

    public static long durationInMinutes(long start, long end){
        Duration duration = new Duration(start, end);
        return duration.getStandardMinutes();
    }

    public static long plusMonth(Calendar calendar, int months) {
        DateTime start = getJodaDateTime(calendar);
        return start.plusMonths(months).getMillis();
    }

    public static long plusDay(Calendar calendar, int days) {
        DateTime start = getJodaDateTime(calendar);
        return start.plusDays(days).getMillis();
    }

    public static long plusYears(Calendar calendar, int years) {
        DateTime start = getJodaDateTime(calendar);
        return start.plusYears(years).getMillis();
    }

    public static DateTime getJodaDateTime(Calendar calendar) {
        return new DateTime(calendar.getTimeInMillis());
    }

    public static long plusHours(Calendar calendar, int hours) {
        return getJodaDateTime(calendar).plusHours(hours).getMillis();
    }

    public static String getDateTime() {
        return (new SimpleDateFormat("dd MMM yyyy HH:mm:ss.sss", Locale.getDefault())).format(new Date(System.currentTimeMillis()));
    }

    public static void addMonth(Calendar calendar, int amount) {
        calendar.add(Calendar.MONTH, amount);
    }

    public static void addYear(Calendar calendar, int amount) {
        calendar.add(Calendar.YEAR, amount);
    }

    public static void addDay(Calendar calendar, int amount) {
        calendar.add(Calendar.DATE, amount);
    }

    public static void addHour(Calendar calendar, int amount) {
        calendar.add(Calendar.HOUR, amount);
    }

    public static void addMinute(Calendar calendar, int amount) {
        calendar.add(Calendar.MINUTE, amount);
    }

    public static void addSeconds(Calendar calendar, int amount) {
        calendar.add(Calendar.SECOND, amount);
    }

    public static void addMSeconds(Calendar calendar, int amount) {
        calendar.add(Calendar.MILLISECOND, amount);
    }

    public static boolean isToday(long currentDateTime) {
        return DateUtils.isToday(currentDateTime);
    }

    private static void setCalendarToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static void setCalendarToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 59);
    }

    public static Date getFirstDayOfWeek(@NonNull Date date, int firstDayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.HOUR);
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            calendar.add(Calendar.DATE, -1);
        }
        return calendar.getTime();
    }

    public static Date getLastDayOfWeek(@Nullable Date date, int firstWeekDay) {
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.HOUR);
        if (calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                && calendar.get(Calendar.DAY_OF_WEEK) == firstWeekDay) {
            return calendar.getTime();
        }
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK));
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstWeekDay) {
            calendar.add(Calendar.DATE, 1);
        }
        return calendar.getTime();
    }

    public static String getDateTime(@Nullable String timestamp, String formatIn, String formatOut) {
        return getDateTime(timestamp, formatIn, formatOut, false,null);
    }

    public static String getDateTime(@Nullable String timestamp, String formatIn, String formatOut, boolean useUTC,@Nullable TimeZone timeZone) {
        long time = convertTimeStringToLong(timestamp, formatIn, timeZone);
        return getDateTime(time, formatOut, useUTC);
    }

    public static String getDateTime(@Nullable Long timestamp, String format) {
        if (timestamp == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getDateTime(@Nullable Long timestamp) {
        if (timestamp == null) {
            return "";
        }
        Date d = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS", Locale.getDefault());
        return sdf.format(d);
    }

    public static String getDateTime(long timestamp, String format) {
        return getDateTime(timestamp, format, false);
    }

    public static String getDateTime(long timestamp,String format,boolean useUTC) {
        Date d = new Date(timestamp);
        Locale locale = Locale.getDefault();
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        if (locale.getISO3Language().equalsIgnoreCase("rus")) {
            DateFormatSymbols formatSimbols = myDateFormatSymbolsFull;
            sdf.setDateFormatSymbols(formatSimbols);
        }
        if (useUTC) {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return sdf.format(d);
    }

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

    public static String getDateTime(int day,int month,int year, String format) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year,month,day);
            long milliseconds = calendar.getTimeInMillis();
            format = (format == null || format.trim().equals("")) ? "dd MMM yyyy HH:mm:ss.sss" : format;
            return (new SimpleDateFormat(format, Locale.getDefault())).format(new Date(milliseconds));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * получение даты DateTime
     *
     * @param date
     * @param format
     * @return DateTime
     */
    public static DateTime getJodaDateTime(String date, String format,boolean useUTC) {
        DateTime dateTime = DateTimeFormat.forPattern(format).parseDateTime(date);
        if (useUTC) {
            dateTime.withZone(DateTimeZone.UTC);
        }
        return dateTime;
    }

    public static long convertDateTimeToLong(int day, int month, int year, boolean useUTC) {
        DateTime dateTime = new DateTime(year, month, day, 0, 0);
        if (useUTC) {
            dateTime.withZone(DateTimeZone.UTC);
        }
        return dateTime.getMillis();
    }

    /**
     * получение даты String
     *
     * @param dateTime
     * @param format
     * @return String
     */
    public static String getDateTime(DateTime dateTime, String format) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
        return fmt.print(dateTime);
    }

    /**
     * Convert string timestamp to long
     * @param myTimestamp String
     * @param format String
     * @return long
     */
    public static long convertTimeStringToLong(String myTimestamp, String format) {
        return convertTimeStringToLong(myTimestamp, format, false);
    }

    public static long convertTimeStringToLong(int day,int month,int year, String format, boolean useUTC) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        if (useUTC) {
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return calendar.getTimeInMillis();
    }

    /**
     * Convert string timestamp to long
     * @param myTimestamp String
     * @param format String
     * @param useUTC
     * @return long
     */
    public static long convertTimeStringToLong(String myTimestamp, String format, boolean useUTC) {
        DateFormatSymbols formatSimbols = getFormatString(myTimestamp);
        Locale locale = getLocale(myTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        if (locale.getISO3Language().equalsIgnoreCase("rus")) {
            sdf.setDateFormatSymbols(formatSimbols);
        }
        if (useUTC) {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        Date date;
        try {
            date = sdf.parse(myTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        return date != null ? date.getTime() : -1;
    }


    public static long convertTimeStringToLong(String myTimestamp, String format,@Nullable TimeZone timeZone ) {
        DateFormatSymbols formatSimbols = getFormatString(myTimestamp);
        Locale locale = getLocale(myTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        if (locale.getISO3Language().equalsIgnoreCase("rus")) {
            sdf.setDateFormatSymbols(formatSimbols);
        }
        if (timeZone != null) {
            sdf.setTimeZone(timeZone);
        }
        Date date;
        try {
            date = sdf.parse(myTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        return date.getTime();
    }

    public static long convertTimeStringToLong(String myTimestamp) {
        DateFormatSymbols formatSimbols = getFormatString(myTimestamp);
        Locale locale = getLocale(myTimestamp);
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormatChooser(myTimestamp), locale);
        if (locale.getCountry().equals("RU")) {
            formatter.setDateFormatSymbols(formatSimbols);
        }
        Date date;
        try {
            date = formatter.parse(myTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        return date.getTime();
    }

    private static DateFormatSymbols getFormatString(String myTimestamp) {
        DateFormatSymbols formatSymbols = myDateFormatSymbolsFull;
        for (String s : RU_MONTHES_LO) {
            if (matcher(".*[\\s+|\\d|-]"+s+"[\\s+|\\d|-].*", myTimestamp)) {
                return myDateFormatSymbols;
            }
        }
        for (String s : RU_MONTHES_LO_EXT) {
            if (matcher(".*[\\s+|\\d|-]"+s+"[\\s+|\\d|-].*", myTimestamp)) {
                return myDateFormatSymbolsExt;
            }
        }
        for (String s : RU_MONTHES_UP) {
            if (matcher(".*[\\s+|\\d|-]"+s+"[\\s+|\\d|-].*", myTimestamp)) {
                return myDateFormatSymbolsUp;
            }
        }
        for (String s : RU_MONTHES_FULL) {
            if (matcher(".*[\\s+|\\d|-]"+s+"[\\s+|\\d|-].*", myTimestamp)) {
                return myDateFormatSymbolsFull;
            }
        }
        return formatSymbols;
    }

    public static int logTimeMinutes(int hh, int mm) {
        return (hh * 60) + mm;
    }

    public static int convertStringToTime(String time) {
     int hours = 0;
        int mins = 0;
        String delimeter = (time.contains(TIME_SEPARATOR_TWICE_DOT)) ? TIME_SEPARATOR_TWICE_DOT : TIME_SEPARATOR_DOT;
        int posDelim = time.indexOf(delimeter);
        try {
            hours = Integer.parseInt(time.substring(0, posDelim));
            mins = Integer.parseInt(time.substring(posDelim + 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mins + (hours * 60);
    }


    public static String strLogTime(int logtime) {
        int h = logtime / 60;
        int m = logtime % 60;
        return pad(h) + TIME_SEPARATOR_TWICE_DOT + pad(m);
    }

    /**
     * convert mins to hh:min
     * @param minutes
     * @return
     */
    public static String getHHmmFromMins(int minutes) {
        int hours = 0;
        int mins = 0;
        try {
            hours = minutes / 60;
            mins = minutes % 60;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hours + ":" + pad(mins);
    }

    public static String pad(long number) {
        if (number < 10) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

    public static String pad(int number) {
        if (number < 10) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

}
