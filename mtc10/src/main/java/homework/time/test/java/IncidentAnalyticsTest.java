import homework.time.Incident;
import homework.time.IncidentAnalytics;
import homework.time.IncidentParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IncidentAnalyticsTest {

    private static final Instant T0 = Instant.parse("2024-01-01T00:00:00Z");

    private static Incident inc(String id, Instant start, Duration d) {
        return new Incident(id, "svc", start, start.plus(d));
    }

    @Test
    void totalAndAverageOnEmptyList() {
        Assertions.assertEquals(Duration.ZERO, IncidentAnalytics.totalDowntime(List.of()));
        assertEquals(Duration.ZERO, IncidentAnalytics.averageDowntime(List.of()));
        assertEquals(Optional.empty(), IncidentAnalytics.longestIncident(List.of()));
    }

    @Test
    void totalAndAverageRoundDown() {
        List<Incident> list = List.of(
                inc("A", T0, Duration.ofSeconds(10)),
                inc("B", T0.plusSeconds(100), Duration.ofSeconds(10)),
                inc("C", T0.plusSeconds(200), Duration.ofSeconds(11)));

        assertEquals(Duration.ofSeconds(31), IncidentAnalytics.totalDowntime(list));
        assertEquals(Duration.ofSeconds(10, 333_333_333), IncidentAnalytics.averageDowntime(list));
    }

    @Test
    void longestIncidentPicksMaxDuration() {
        Incident shortOne = inc("A", T0, Duration.ofMinutes(5));
        Incident longOne = inc("B", T0.plusSeconds(10), Duration.ofMinutes(50));

        assertEquals(Optional.of(longOne),
                IncidentAnalytics.longestIncident(List.of(shortOne, longOne)));
    }

    @Test
    void longestIncidentTieBreaksByEarlierStartThenId() {
        Incident later = inc("A", T0.plusSeconds(60), Duration.ofMinutes(10));
        Incident earlier = inc("Z", T0, Duration.ofMinutes(10));
        assertEquals(Optional.of(earlier), IncidentAnalytics.longestIncident(List.of(later, earlier)));

        Incident idB = inc("B", T0, Duration.ofMinutes(10));
        Incident idA = inc("A", T0, Duration.ofMinutes(10));
        assertEquals(Optional.of(idA), IncidentAnalytics.longestIncident(List.of(idB, idA)));
    }

    @Test
    void groupsByLocalDateInBerlinWhereItDiffersFromUtc() {
        ZoneId berlin = ZoneId.of("Europe/Berlin");
        Incident inc1 = IncidentParser.parse("INC-1;billing;2024-03-30T22:30:00Z;2024-03-31T01:00:00Z");
        Incident inc2 = IncidentParser.parse("INC-2;gateway;2024-03-30T23:30:00Z;2024-03-31T00:15:00Z");
        Incident inc0 = IncidentParser.parse("INC-0;auth;2024-03-30T23:45:00Z;2024-03-31T00:00:00Z");

        Map<LocalDate, List<Incident>> grouped =
                IncidentAnalytics.groupByStartDate(List.of(inc2, inc0, inc1), berlin);


        assertEquals(List.of(LocalDate.of(2024, 3, 30), LocalDate.of(2024, 3, 31)),
                new ArrayList<>(grouped.keySet()));
        assertEquals(List.of(inc1), grouped.get(LocalDate.of(2024, 3, 30)));
        assertEquals(List.of(inc2, inc0), grouped.get(LocalDate.of(2024, 3, 31)));
    }

    @Test
    void doesNotMutateInput() {
        List<Incident> input = new ArrayList<>(List.of(
                inc("B", T0.plusSeconds(10), Duration.ofSeconds(1)),
                inc("A", T0, Duration.ofSeconds(1))));
        List<Incident> copy = List.copyOf(input);

        IncidentAnalytics.groupByStartDate(input, ZoneId.of("UTC"));
        IncidentAnalytics.longestIncident(input);

        assertEquals(copy, input);
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> IncidentAnalytics.totalDowntime(null));
        assertThrows(NullPointerException.class,
                () -> IncidentAnalytics.groupByStartDate(List.of(), null));
    }
}