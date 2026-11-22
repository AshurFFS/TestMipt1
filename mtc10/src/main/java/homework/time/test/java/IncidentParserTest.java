import homework.time.Incident;
import homework.time.IncidentParser;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IncidentParserTest {

    @Test
    void parsesValidLineAndComputesDuration() {
        Incident incident = IncidentParser.parse("INC-1;billing;2024-03-30T22:30:00Z;2024-03-31T01:00:00Z");

        assertEquals("INC-1", incident.id());
        assertEquals("billing", incident.service());
        assertEquals(Instant.parse("2024-03-30T22:30:00Z"), incident.startedAt());
        assertEquals(Instant.parse("2024-03-31T01:00:00Z"), incident.resolvedAt());
        assertEquals(Duration.ofMinutes(150), incident.duration());
    }

    @Test
    void rejectsInvalidTimestamp() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> IncidentParser.parse("INC-3;billing;not-a-time;2024-03-31T01:00:00Z"));
        assertTrue(e.getMessage().contains("startedAt"));
    }

    @Test
    void rejectsLocalTimestampWithoutOffset() {
        assertThrows(IllegalArgumentException.class,
                () -> IncidentParser.parse("INC-3;billing;2024-03-30T22:30:00;2024-03-31T01:00:00Z"));
    }

    @Test
    void rejectsEmptyId() {
        assertThrows(IllegalArgumentException.class,
                () -> IncidentParser.parse(";billing;2024-03-30T22:30:00Z;2024-03-31T01:00:00Z"));
    }

    @Test
    void rejectsEmptyService() {
        assertThrows(IllegalArgumentException.class,
                () -> IncidentParser.parse("INC-4; ;2024-03-30T22:30:00Z;2024-03-31T01:00:00Z"));
    }

    @Test
    void rejectsResolvedBeforeStarted() {
        assertThrows(IllegalArgumentException.class,
                () -> IncidentParser.parse("INC-5;billing;2024-03-31T01:00:00Z;2024-03-30T22:30:00Z"));
    }

    @Test
    void rejectsWrongFieldCount() {
        assertThrows(IllegalArgumentException.class,
                () -> IncidentParser.parse("INC-6;billing;2024-03-30T22:30:00Z"));
        assertThrows(IllegalArgumentException.class,
                () -> IncidentParser.parse("INC-6;billing;2024-03-30T22:30:00Z;2024-03-31T01:00:00Z;extra"));
    }

    @Test
    void rejectsNullLine() {
        assertThrows(NullPointerException.class, () -> IncidentParser.parse(null));
    }
}