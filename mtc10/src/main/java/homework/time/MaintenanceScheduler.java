package homework.time;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MaintenanceScheduler {

    private final Clock clock;

    public MaintenanceScheduler(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public MaintenanceWindow nextWindow(
            LocalDate date, LocalTime localStart, Duration duration,
            ZoneId zone, OverlapPolicy policy) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(localStart, "localStart must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        Objects.requireNonNull(policy, "policy must not be null");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("duration must be positive: " + duration);
        }

        LocalDateTime local = LocalDateTime.of(date, localStart);

        if (zone.getRules().getValidOffsets(local).isEmpty()) {
            throw new DateTimeException(
                    "Local time " + local + " does not exist in zone " + zone + " (DST gap)");
        }

        ZonedDateTime zoned = ZonedDateTime.of(local, zone);
        zoned = switch (policy) {
            case EARLIER -> zoned.withEarlierOffsetAtOverlap();
            case LATER -> zoned.withLaterOffsetAtOverlap();
        };

        Instant start = zoned.toInstant();
        Instant end = start.plus(duration);
        return new MaintenanceWindow(start, end);
    }

    public List<MaintenanceWindow> upcomingWindows(
            int days, LocalTime localStart, Duration duration,
            ZoneId zone, OverlapPolicy policy) {
        Objects.requireNonNull(zone, "zone must not be null");
        if (days < 0) {
            throw new IllegalArgumentException("days must not be negative: " + days);
        }
        List<MaintenanceWindow> result = new ArrayList<>(days);
        if (days == 0) {
            return result;
        }
        LocalDate today = LocalDate.now(clock.withZone(zone));
        for (int i = 0; i < days; i++) {
            result.add(nextWindow(today.plusDays(i), localStart, duration, zone, policy));
        }
        return result;
    }
}