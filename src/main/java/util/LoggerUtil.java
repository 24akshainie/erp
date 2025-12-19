package util;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {

    private static final String LOG_FILE = "erp_log.txt";
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Write info-level message.
     */
    public static void info(String msg) {
        log("INFO", msg);
    }

    /**
     * Write debug-level message.
     */
    public static void debug(String msg) {
        log("DEBUG", msg);
    }

    /**
     * Write error-level message.
     */
    public static void error(String msg) {
        log("ERROR", msg);
    }

    /**
     * Core logger: prints to console + writes to file.
     */
    private static void log(String level, String msg) {
        String timestamp = LocalDateTime.now().format(FORMAT);
        String fullMsg = "[" + timestamp + "] [" + level + "] " + msg;

        // Console output
        System.out.println(fullMsg);

        // File output
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(fullMsg);
        } catch (Exception e) {
            System.out.println("[LoggerUtil] Failed to write log: " + e.getMessage());
        }
    }
}
