package vn.androidhaui.foxtrip.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for date and time formatting.
 * Standardizes UTC for backend and GMT+7 for UI display.
 */
public class DateTimeUtils {

    private static final String DISPLAY_FORMAT_FULL = "dd/MM/yyyy HH:mm";
    private static final String DISPLAY_FORMAT_DATE = "dd/MM/yyyy";
    private static final String BACKEND_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String TIMEZONE_VN = "GMT+7";

    private static SimpleDateFormat getFormatter(String pattern, String timezone) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        return sdf;
    }

    /**
     * dd/MM/yyyy HH:mm (GMT+7)
     */
    public static String toDisplayString(Date date) {
        if (date == null) return "-";
        return getFormatter(DISPLAY_FORMAT_FULL, TIMEZONE_VN).format(date);
    }
    
    /**
     * dd/MM/yyyy (GMT+7)
     */
    public static String toDisplayDate(Date date) {
        if (date == null) return "-";
        return getFormatter(DISPLAY_FORMAT_DATE, TIMEZONE_VN).format(date);
    }

    /**
     * Parses UI string to Date (GMT+7)
     */
    public static Date fromDisplayString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return getFormatter(DISPLAY_FORMAT_FULL, TIMEZONE_VN).parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Robust parser for backend strings (ISO-8601 UTC)
     */
    public static Date parseBackendDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        };
        
        for (String pattern : patterns) {
            try {
                String tz = pattern.endsWith("'Z'") ? "UTC" : TIMEZONE_VN;
                return getFormatter(pattern, tz).parse(dateStr);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    /**
     * yyyy-MM-dd'T'HH:mm:ss.SSS'Z' (UTC)
     */
    public static String formatForBackend(Date date) {
        if (date == null) return null;
        return getFormatter(BACKEND_FORMAT, "UTC").format(date);
    }

    /**
     * Hôm nay, Ngày mai, Hôm qua... or dd/MM/yyyy (GMT+7)
     */
    public static String toRelativeDateString(Date date) {
        if (date == null) return "-";
        
        TimeZone tz = TimeZone.getTimeZone(TIMEZONE_VN);
        Calendar calDate = Calendar.getInstance(tz);
        calDate.setTime(date);
        
        Calendar calNow = Calendar.getInstance(tz);
        
        if (isSameDay(calDate, calNow)) return "Hôm nay";
        
        Calendar calRef = (Calendar) calNow.clone();
        calRef.add(Calendar.DAY_OF_YEAR, 1);
        if (isSameDay(calDate, calRef)) return "Ngày mai";
        
        calRef = (Calendar) calNow.clone();
        calRef.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(calDate, calRef)) return "Hôm qua";
        
        return toDisplayDate(date);
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
