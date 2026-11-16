package validation;

import java.util.Objects;

/**
 * Неизменяемое описание одного нарушения ограничения.
 *
 * @param fieldName      имя поля
 * @param annotationName простое имя аннотации без символа @
 * @param message        сообщение из аннотации
 */
public record ValidationError(String fieldName, String annotationName, String message) {

    public ValidationError {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(annotationName, "annotationName must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
