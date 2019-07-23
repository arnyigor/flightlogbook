package com.arny.helpers.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class DateTimeUtils {

    private static final String[] RU_MONTHES_FULL = new String[]{"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};
    private static final String[] RU_MONTHES_FULL_EXT = new String[]{"январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"};
    private static final String[] RU_MONTHES_LO = new String[]{"янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек",};
    private static final String[] RU_MONTHES_LO_EXT = new String[]{"янв", "февр", "мар", "апр", "май", "июн", "июл", "авг", "сент", "окт", "нояб", "дек",};
    private static final String[] RU_MONTHES_UP = new String[]{"Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};
    private static final String TIME_SEPARATOR_TWICE_DOT = ":";
    private static final String TIME_SEPARATOR_DOT = ".";

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
                System.out.println("dateFormatChooser myTimestamp:" + myTimestamp + ": format:" + value);
//				Log.d(DateTimeUtils.class.getSimpleName(), "dateFormatChooser myTimestamp:" + myTimestamp + ": format:" + value);
                return value;
            }
        }
        System.out.println("dateFormatChooser myTimestamp:" + myTimestamp + ": format:" + format);
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

    public static String getDateTime() {
        return (new SimpleDateFormat("dd MMM yyyy HH:mm:ss.sss", Locale.getDefault())).format(new Date(System.currentTimeMillis()));
    }

    public static String getDateTime(String format) {
        return (new SimpleDateFormat(format, Locale.getDefault())).format(new Date(System.currentTimeMillis()));
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

    public static long convertTimeStringToLong(String myTimestamp, String format) {
        DateFormatSymbols formatSimbols = getFormatString(myTimestamp);
        Locale locale = getLocale(myTimestamp);
        SimpleDateFormat formatter = new SimpleDateFormat(format, locale);
        if (locale.getISO3Language().equalsIgnoreCase("rus")) {
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
            if (Utility.matcher(".*[\\s+|\\d|-]" + s + "[\\s+|\\d|-].*", myTimestamp)) {
                return myDateFormatSymbols;
            }
        }
        for (String s : RU_MONTHES_LO_EXT) {
            if (Utility.matcher(".*[\\s+|\\d|-]" + s + "[\\s+|\\d|-].*", myTimestamp)) {
                return myDateFormatSymbolsExt;
            }
        }
        for (String s : RU_MONTHES_UP) {
            if (Utility.matcher(".*[\\s+|\\d|-]" + s + "[\\s+|\\d|-].*", myTimestamp)) {
                return myDateFormatSymbolsUp;
            }
        }
        for (String s : RU_MONTHES_FULL) {
            if (Utility.matcher(".*[\\s+|\\d|-]" + s + "[\\s+|\\d|-].*", myTimestamp)) {
                return myDateFormatSymbolsFull;
            }
        }
        return formatSymbols;
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

    public static String strLogTime(int logtime) {
        int h = logtime / 60;
        int m = logtime % 60;
        return pad(h) + TIME_SEPARATOR_TWICE_DOT + pad(m);
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
            mins = Integer.parseInt(time.substring(posDelim + 1, time.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mins + (hours * 60);
    }

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

    /**
     * add '0' to number before 10
     *
     * @param number
     * @return
     */
    public static String pad(int number) {
        if (number >= 10) {
            return String.valueOf(number);
        } else {
            return "0" + String.valueOf(number);
        }
    }

    public static String getStringDateTime(int year, int monthOfYear, int dayOfMonth) {
        String strDateFormat = "MMM";
        String strMonth = new DateFormatSymbols().getMonths()[monthOfYear];
        Date date = null;
        try {
            date = new SimpleDateFormat(strDateFormat, Locale.getDefault()).parse(strMonth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formDate = new SimpleDateFormat("MMM", Locale.getDefault()).format(date);
        return dayOfMonth + " " + formDate + " " + year;
    }

    public static int getMonth(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static String getStrMonth(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        String strDateFormat = "MMM";
        String strM = new DateFormatSymbols().getMonths()[m];
        Date dat = null;
        try {
            dat = new SimpleDateFormat(strDateFormat, Locale.getDefault()).parse(strM);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String mMonth = new SimpleDateFormat("MMM", Locale.getDefault()).format(dat);
        return mMonth + " " + y;
    }

    public static String getStrDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        String strDateFormat = "MMM";
        String strM = new DateFormatSymbols().getMonths()[m];
        Date dat = null;
        try {
            dat = new SimpleDateFormat(strDateFormat, Locale.getDefault()).parse(strM);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String fDate = new SimpleDateFormat("MMM", Locale.getDefault()).format(dat);
        return d + " " + fDate + " " + y;
    }

    public static String getStrTime(long timestamp) {
        Date d = new Date(timestamp);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm", Locale.getDefault());
        return format.format(d);
    }

    public static String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        String strDateFormat = "MMM";
        String strM = new DateFormatSymbols().getMonths()[m];
        Date dat = null;
        try {
            dat = new SimpleDateFormat(strDateFormat, Locale.getDefault()).parse(strM);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String fDate = new SimpleDateFormat("MMM", Locale.getDefault()).format(dat);
        return d + " " + fDate + " " + y;
    }

    /**
     * получение даты DateTime
     *
     * @param date
     * @param format
     * @return DateTime
     */
    public static DateTime getDateTime(String date, String format) {
        return DateTimeFormat.forPattern(format).parseDateTime(date);
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

    public static String getDateTime(long milliseconds, String format) {
        DateTime dateTime = new DateTime().withMillis(milliseconds);
        return getDateTime(dateTime, format);
    }

    public static String getDateTime(long milliseconds) {
        DateTime dateTime = new DateTime().withMillis(milliseconds);
        return getDateTime(dateTime, "dd MMM yyyy HH:mm:ss.sss");
    }

    /**
     * Конвертирование из одного формата в другой
     *
     * @param dateTimeFrom
     * @param formatfrom
     * @param formatTo
     * @return
     */
    public static String getDateTime(String dateTimeFrom, String formatfrom, String formatTo) {
        return getDateTime(getDateTime(dateTimeFrom, formatfrom), formatTo);
    }

    public static double getTimeDiff(long starttime) {
        return (double) (System.currentTimeMillis() - starttime) / 1000;
    }
}