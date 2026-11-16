package validation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Декларативный валидатор объектов на основе аннотаций и Reflection API.
 */
public final class Validator {

    private Validator() {
    }

    public static ValidationResult validate(Object object) {
        Objects.requireNonNull(object, "object must not be null");

        List<ValidationError> errors = new ArrayList<>();
        for (Class<?> type = object.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                validateField(object, field, errors);
            }
        }
        return new ValidationResult(errors);
    }

    private static void validateField(Object object, Field field, List<ValidationError> errors) {
        NotNull notNull = field.getAnnotation(NotNull.class);
        Size size = field.getAnnotation(Size.class);
        Range range = field.getAnnotation(Range.class);
        Email email = field.getAnnotation(Email.class);

        if (notNull == null && size == null && range == null && email == null) {
            return;
        }

        checkConfiguration(field, size, range, email);

        Object value = readValue(object, field);
        String name = field.getName();

        if (notNull != null && value == null) {
            errors.add(new ValidationError(name, NotNull.class.getSimpleName(), notNull.message()));
        }
        if (value == null) {
            return;
        }

        if (size != null) {
            int length = ((CharSequence) value).length();
            if (length < size.min() || length > size.max()) {
                errors.add(new ValidationError(name, Size.class.getSimpleName(), size.message()));
            }
        }
        if (range != null) {
            double number = ((Number) value).doubleValue();
            if (Double.isNaN(number) || number < range.min() || number > range.max()) {
                errors.add(new ValidationError(name, Range.class.getSimpleName(), range.message()));
            }
        }
        if (email != null && !isValidEmail((CharSequence) value)) {
            errors.add(new ValidationError(name, Email.class.getSimpleName(), email.message()));
        }
    }

    private static void checkConfiguration(Field field, Size size, Range range, Email email) {
        Class<?> declaredType = field.getType();

        if (size != null) {
            if (!CharSequence.class.isAssignableFrom(declaredType)) {
                throw unsupportedType(field, Size.class);
            }
            if (size.min() < 0 || size.min() > size.max()) {
                throw new ValidationConfigurationException(
                        "Invalid @Size bounds on " + describe(field) + ": min=" + size.min() + ", max=" + size.max());
            }
        }
        if (range != null) {
            if (!isNumericType(declaredType)) {
                throw unsupportedType(field, Range.class);
            }
            double min = range.min();
            double max = range.max();
            if (Double.isNaN(min) || Double.isNaN(max) || Double.isInfinite(min) || Double.isInfinite(max)
                    || min > max) {
                throw new ValidationConfigurationException(
                        "Invalid @Range bounds on " + describe(field) + ": min=" + min + ", max=" + max);
            }
        }
        if (email != null && !CharSequence.class.isAssignableFrom(declaredType)) {
            throw unsupportedType(field, Email.class);
        }
    }

    private static boolean isNumericType(Class<?> type) {
        return type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == float.class
                || type == double.class
                || Number.class.isAssignableFrom(type);
    }

    private static ValidationConfigurationException unsupportedType(Field field, Class<?> annotation) {
        return new ValidationConfigurationException(
                "@" + annotation.getSimpleName() + " is not supported on " + describe(field)
                        + " of type " + field.getType().getName());
    }

    private static Object readValue(Object object, Field field) {
        boolean accessible;
        try {
            accessible = field.trySetAccessible();
        } catch (SecurityException e) {
            throw new ValidationConfigurationException("Field is not accessible: " + describe(field), e);
        }
        if (!accessible) {
            throw new ValidationConfigurationException("Field is not accessible: " + describe(field));
        }
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new ValidationConfigurationException("Cannot read field: " + describe(field), e);
        }
    }

    private static boolean isValidEmail(CharSequence value) {
        String text = value.toString();
        if (text.isBlank()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }
        int at = text.indexOf('@');
        if (at <= 0 || at != text.lastIndexOf('@') || at == text.length() - 1) {
            return false;
        }
        return text.indexOf('.', at + 1) >= 0;
    }

    private static String describe(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }
}
