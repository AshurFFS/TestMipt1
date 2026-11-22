package homework.time;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public final class IncidentAnalytics {

    private static final BigInteger NANOS_IN_SECOND = BigInteger.valueOf(1_000_000_000L);
    private static final Comparator<Incident> BY_START_THEN_ID =
            Comparator.comparing(Incident::startedAt).thenComparing(Incident::id);

    private IncidentAnalytics() {
    }

    public static Duration totalDowntime(List<Incident> incidents) {
        requireValid(incidents);
        Duration total = Duration.ZERO;
        for (Incident incident : incidents) {
            total = total.plus(incident.duration());
        }
        return total;
    }

    public static Duration averageDowntime(List<Incident> incidents) {
        requireValid(incidents);
        if (incidents.isEmpty()) {
            return Duration.ZERO;
        }
        BigInteger totalNanos = BigInteger.ZERO;
        for (Incident incident : incidents) {
            Duration d = incident.duration();
            BigInteger nanos = BigInteger.valueOf(d.getSeconds())
                    .multiply(NANOS_IN_SECOND)
                    .add(BigInteger.valueOf(d.getNano()));
            totalNanos = totalNanos.add(nanos);
        }
        // Длительности неотрицательны, поэтому усечение делением = округление вниз.
        BigInteger avgNanos = totalNanos.divide(BigInteger.valueOf(incidents.size()));
        BigInteger[] secAndNanos = avgNanos.divideAndRemainder(NANOS_IN_SECOND);
        return Duration.ofSeconds(secAndNanos[0].longValueExact(), secAndNanos[1].longValueExact());
    }

    public static Optional<Incident> longestIncident(List<Incident> incidents) {
        requireValid(incidents);
        Incident best = null;
        for (Incident candidate : incidents) {
            if (best == null || isBetter(candidate, best)) {
                best = candidate;
            }
        }
        return Optional.ofNullable(best);
    }

    private static boolean isBetter(Incident candidate, Incident current) {
        int byDuration = candidate.duration().compareTo(current.duration());
        if (byDuration != 0) {
            return byDuration > 0;
        }
        int byStart = candidate.startedAt().compareTo(current.startedAt());
        if (byStart != 0) {
            return byStart < 0;
        }
        return candidate.id().compareTo(current.id()) < 0;
    }

    public static Map<LocalDate, List<Incident>> groupByStartDate(
            List<Incident> incidents, ZoneId zone) {
        requireValid(incidents);
        Objects.requireNonNull(zone, "zone must not be null");

        Map<LocalDate, List<Incident>> result = new TreeMap<>();
        for (Incident incident : incidents) {
            LocalDate date = incident.startedAt().atZone(zone).toLocalDate();
            result.computeIfAbsent(date, d -> new ArrayList<>()).add(incident);
        }
        for (List<Incident> group : result.values()) {
            group.sort(BY_START_THEN_ID);
        }
        return result;
    }

    private static void requireValid(List<Incident> incidents) {
        Objects.requireNonNull(incidents, "incidents must not be null");
        for (Incident incident : incidents) {
            Objects.requireNonNull(incident, "incidents must not contain null elements");
        }
    }
}