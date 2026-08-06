package Util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter DATETIME_FORMAT  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FORMAT   = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private DateUtil() {}

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMAT);
    }

    public static String getCurrentDateDisplay() { return LocalDate.now().format(DISPLAY_FORMAT);}
}
