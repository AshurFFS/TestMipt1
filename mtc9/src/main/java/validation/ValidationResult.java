package validation;

import java.util.List;

/**
 * Неизменяемый результат проверки объекта.
 */
public final class ValidationResult {

    private final List<ValidationError> errors;

    public ValidationResult(List<ValidationError> errors) {
        if (errors == null) {
            throw new NullPointerException("errors must not be null");
        }
        this.errors = List.copyOf(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
