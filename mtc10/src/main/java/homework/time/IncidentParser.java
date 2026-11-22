package homework.time;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public final class IncidentParser {

    private IncidentParser() {
    }

    public static Incident parse(String line) {
        Objects.requireNonNull(line, "line must not be null");

        String[] parts = line.split(";", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Expected 4 fields (id;service;startedAt;resolvedAt) but got " + parts.length + ": " + line);
        }

        String id = requireNotBlank(parts[0], "id", line);
        String service = requireNotBlank(parts[1], "service", line);
        Instant startedAt = parseInstant(parts[2], "startedAt", line);
        Instant resolvedAt = parseInstant(parts[3], "resolvedAt", line);

        return new Incident(id, service, startedAt, resolvedAt);
    }

    private static String requireNotBlank(String value, String field, String line) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Field '" + field + "' must not be empty: " + line);
        }
        return trimmed;
    }

    private static Instant parseInstant(String value, String field, String line) {
        String trimmed = requireNotBlank(value, field, line);
        try {
            return Instant.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Field '" + field + "' is not a valid Instant: '" + trimmed + "'", e);
        }
    }
}