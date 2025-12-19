package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeHelper {

    // Default formats (you can change them)
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // -------------------------------------------------------------------
    // Current Date / Time
    // -------------------------------------------------------------------

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    public static String getCurrentTime() {
        return LocalTime.now().format(TIME_FORMAT);
    }

    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DATE_TIME_FORMAT);
    }

    // -------------------------------------------------------------------
    // Formatting
    // -------------------------------------------------------------------

    public static String format(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    public static String format(LocalTime time) {
        return time.format(TIME_FORMAT);
    }

    public static String format(LocalDateTime dt) {
        return dt.format(DATE_TIME_FORMAT);
    }

    // -------------------------------------------------------------------
    // Parsing utilities (safe)
    // -------------------------------------------------------------------

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (Exception e) {
            return null; // return null if invalid
        }
    }

    public static LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, TIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime parseDateTime(String ts) {
        try {
            return LocalDateTime.parse(ts, DATE_TIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
}
