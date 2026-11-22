import homework.time.*;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ReportAndLegacyTest {

    @Test
    void formatsWindowExactly() {
        MaintenanceWindow window = new MaintenanceWindow(
                Instant.parse("2024-03-31T01:00:00Z"),
                Instant.parse("2024-03-31T02:00:00Z"));

        String text = ReportFormatter.formatWindow(window, ZoneId.of("Europe/Berlin"), Locale.ROOT);

        assertEquals("31.03.2024 03:00 +02:00 (Europe/Berlin) – 31.03.2024 04:00 +02:00 (Europe/Berlin)", text);
    }

    @Test
    void instantDateRoundTrip() {
        Instant instant = Instant.parse("2024-03-31T01:00:00.123Z");

        Date date = LegacyDateAdapter.toDate(instant);
        assertEquals(instant.toEpochMilli(), date.getTime());
        assertEquals(instant, LegacyDateAdapter.toInstant(date));

        Date original = new Date(1_711_846_800_000L);
        assertEquals(original, LegacyDateAdapter.toDate(LegacyDateAdapter.toInstant(original)));
    }

    @Test
    void calendarRoundTripKeepsInstantAndZone() {
        ZonedDateTime zdt = ZonedDateTime.of(2024, 10, 27, 12, 0, 0, 0, ZoneId.of("Europe/Berlin"));

        GregorianCalendar calendar = LegacyDateAdapter.toGregorianCalendar(zdt);
        assertEquals(zdt.toInstant().toEpochMilli(), calendar.getTimeInMillis());
        assertEquals("Europe/Berlin", calendar.getTimeZone().getID());

        ZonedDateTime back = LegacyDateAdapter.toZonedDateTime(calendar);
        assertEquals(zdt.toInstant(), back.toInstant());
        assertEquals(zdt.getZone(), back.getZone());
        assertEquals(zdt, back);
    }

    @Test
    void adapterRejectsNull() {
        assertThrows(NullPointerException.class, () -> LegacyDateAdapter.toInstant(null));
        assertThrows(NullPointerException.class, () -> LegacyDateAdapter.toDate(null));
        assertThrows(NullPointerException.class, () -> LegacyDateAdapter.toZonedDateTime(null));
        assertThrows(NullPointerException.class, () -> LegacyDateAdapter.toGregorianCalendar(null));
    }
}