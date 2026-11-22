import homework.time.*;

import org.junit.jupiter.api.Test;


import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceSchedulerTest {

    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    private static MaintenanceScheduler scheduler() {
        return new MaintenanceScheduler(
                Clock.fixed(Instant.parse("2024-03-30T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void rejectsNonexistentLocalTimeInGap() {
        assertThrows(DateTimeException.class, () -> scheduler().nextWindow(
                LocalDate.of(2024, 3, 31), LocalTime.of(2, 30), Duration.ofMinutes(30),
                BERLIN, OverlapPolicy.EARLIER));
        assertThrows(DateTimeException.class, () -> scheduler().nextWindow(
                LocalDate.of(2024, 3, 31), LocalTime.of(2, 30), Duration.ofMinutes(30),
                BERLIN, OverlapPolicy.LATER));
    }

    @Test
    void overlapPoliciesGiveDifferentOffsets() {
        LocalDate date = LocalDate.of(2024, 10, 27);
        LocalTime time = LocalTime.of(2, 30);
        Duration d = Duration.ofMinutes(30);

        MaintenanceWindow earlier = scheduler().nextWindow(date, time, d, BERLIN, OverlapPolicy.EARLIER);
        MaintenanceWindow later = scheduler().nextWindow(date, time, d, BERLIN, OverlapPolicy.LATER);

        assertEquals(ZoneOffset.ofHours(2), earlier.start().atZone(BERLIN).getOffset());
        assertEquals(ZoneOffset.ofHours(1), later.start().atZone(BERLIN).getOffset());
        assertEquals(Instant.parse("2024-10-27T00:30:00Z"), earlier.start());
        assertEquals(Instant.parse("2024-10-27T01:30:00Z"), later.start());
        assertEquals(Duration.ofHours(1), Duration.between(earlier.start(), later.start()));
        assertEquals(d, earlier.duration());
        assertEquals(d, later.duration());
    }

    @Test
    void dailyWindowsKeepLocalTimeAcrossSpringForward() {
        MaintenanceScheduler s = scheduler();
        List<MaintenanceWindow> windows = s.upcomingWindows(
                2, LocalTime.of(3, 30), Duration.ofMinutes(30), BERLIN, OverlapPolicy.EARLIER);

        assertEquals(2, windows.size());
        ZonedDateTime first = windows.get(0).start().atZone(BERLIN);
        ZonedDateTime second = windows.get(1).start().atZone(BERLIN);

        assertEquals(LocalDate.of(2024, 3, 30), first.toLocalDate());
        assertEquals(LocalDate.of(2024, 3, 31), second.toLocalDate());
        assertEquals(LocalTime.of(3, 30), first.toLocalTime());
        assertEquals(LocalTime.of(3, 30), second.toLocalTime());
        assertEquals(Duration.ofHours(23), Duration.between(windows.get(0).start(), windows.get(1).start()));


        assertEquals(windows, s.upcomingWindows(
                2, LocalTime.of(3, 30), Duration.ofMinutes(30), BERLIN, OverlapPolicy.EARLIER));
    }

    @Test
    void zeroDaysReturnsEmptyListAndNegativeFails() {
        assertTrue(scheduler().upcomingWindows(
                0, LocalTime.of(3, 30), Duration.ofMinutes(30), BERLIN, OverlapPolicy.EARLIER).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> scheduler().upcomingWindows(
                -1, LocalTime.of(3, 30), Duration.ofMinutes(30), BERLIN, OverlapPolicy.EARLIER));
    }

    @Test
    void rejectsNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> scheduler().nextWindow(
                LocalDate.of(2024, 3, 30), LocalTime.of(3, 30), Duration.ZERO,
                BERLIN, OverlapPolicy.EARLIER));
        assertThrows(IllegalArgumentException.class, () -> scheduler().nextWindow(
                LocalDate.of(2024, 3, 30), LocalTime.of(3, 30), Duration.ofMinutes(-5),
                BERLIN, OverlapPolicy.EARLIER));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> new MaintenanceScheduler(null));
        assertThrows(NullPointerException.class, () -> scheduler().nextWindow(
                null, LocalTime.of(3, 30), Duration.ofMinutes(30), BERLIN, OverlapPolicy.EARLIER));
    }
}