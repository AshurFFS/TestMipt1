package homework.time;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public final class ReportFormatter {

    private static final String PATTERN = "dd.MM.uuuu HH:mm XXX (VV)";

    private ReportFormatter() {
    }

    public static String formatWindow(MaintenanceWindow window, ZoneId zone, Locale locale) {
        Objects.requireNonNull(window, "window must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN, locale);
        return window.start().atZone(zone).format(formatter)
                + " – "
                + window.end().atZone(zone).format(formatter);
    }
}