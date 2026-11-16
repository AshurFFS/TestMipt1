package validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidatorTest {

    // ---------- модели ----------

    static class User {
        @NotNull(message = "name required")
        @Size(min = 2, max = 50, message = "name size")
        private final String name;

        @NotNull(message = "email required")
        @Email(message = "bad email")
        private final String email;

        @Range(min = 0, max = 150, message = "bad age")
        private final Integer age;

        User(String name, String email, Integer age) {
            this.name = name;
            this.email = email;
            this.age = age;
        }
    }

    static class NameHolder {
        @Size(min = 2, max = 5)
        private final CharSequence value;

        NameHolder(CharSequence value) {
            this.value = value;
        }
    }

    static class AgeHolder {
        @Range(min = 0, max = 150)
        private final Integer value;

        AgeHolder(Integer value) {
            this.value = value;
        }
    }

    static class DoubleHolder {
        @Range(min = 0, max = 1)
        private final Double value;

        DoubleHolder(Double value) {
            this.value = value;
        }
    }

    static class EmailHolder {
        @Email
        private final String value;

        EmailHolder(String value) {
            this.value = value;
        }
    }

    static class NotNullHolder {
        @NotNull
        private final Object value;

        NotNullHolder(Object value) {
            this.value = value;
        }
    }

    static class Primitives {
        @NotNull
        @Range(min = 0, max = 10)
        private final int number;

        Primitives(int number) {
            this.number = number;
        }
    }

    static class Base {
        @NotNull(message = "base")
        private String baseField;
    }

    static class Child extends Base {
        @NotNull(message = "child")
        private String childField;
    }

    static class WithStatic {
        @NotNull
        private static String staticField;

        @NotNull
        private final String instanceField = "ok";
    }

    static class MultiViolation {
        @Size(min = 5, max = 10, message = "size")
        @Email(message = "email")
        private final String value = "ab";
    }

    static class SizeMinGreaterThanMax {
        @Size(min = 5, max = 2)
        private String value;
    }

    static class SizeNegativeMin {
        @Size(min = -1, max = 2)
        private String value;
    }

    static class SizeOnInteger {
        @Size(min = 0, max = 2)
        private Integer value;
    }

    static class RangeMinGreaterThanMax {
        @Range(min = 10, max = 1)
        private int value;
    }

    static class RangeNaNBound {
        @Range(min = Double.NaN, max = 1)
        private int value;
    }

    static class RangeInfiniteBound {
        @Range(min = 0, max = Double.POSITIVE_INFINITY)
        private int value;
    }

    static class RangeOnString {
        @Range(min = 0, max = 1)
        private String value;
    }

    static class EmailOnInteger {
        @Email
        private Integer value;
    }

    // ---------- вспомогательные методы ----------

    private static Set<String> keys(ValidationResult result) {
        return result.getErrors().stream()
                .map(e -> e.fieldName() + ":" + e.annotationName())
                .collect(Collectors.toSet());
    }

    // ---------- сценарии ----------

    @Test
    void validObjectHasNoErrors() {
        ValidationResult result = Validator.validate(new User("Alice", "alice@example.com", 30));

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void notNullDetectsNull() {
        ValidationResult result = Validator.validate(new NotNullHolder(null));

        assertFalse(result.isValid());
        assertEquals(Set.of("value:NotNull"), keys(result));
    }

    @Test
    void sizeDetectsViolation() {
        ValidationResult result = Validator.validate(new NameHolder("a"));

        assertEquals(Set.of("value:Size"), keys(result));
    }

    @Test
    void rangeDetectsViolation() {
        ValidationResult result = Validator.validate(new AgeHolder(200));

        assertEquals(Set.of("value:Range"), keys(result));
    }

    @Test
    void emailDetectsViolation() {
        ValidationResult result = Validator.validate(new EmailHolder("invalid"));

        assertEquals(Set.of("value:Email"), keys(result));
    }

    @Test
    void errorContainsFieldAnnotationAndMessage() {
        ValidationResult result = Validator.validate(new User("Alice", "bad", 30));

        assertEquals(1, result.getErrors().size());
        ValidationError error = result.getErrors().get(0);
        assertEquals("email", error.fieldName());
        assertEquals("Email", error.annotationName());
        assertEquals("bad email", error.message());
    }

    @Test
    void defaultMessageIsUsedWhenNotSpecified() {
        ValidationResult result = Validator.validate(new NotNullHolder(null));

        assertFalse(result.getErrors().get(0).message().isBlank());
    }

    @Test
    void violationsOfDifferentFieldsAreCollectedInOneCall() {
        ValidationResult result = Validator.validate(new User("A", "invalid-email", 200));

        assertEquals(3, result.getErrors().size());
        assertEquals(Set.of("name:Size", "email:Email", "age:Range"), keys(result));
    }

    @Test
    void nullValuesAreSkippedByOptionalConstraints() {
        assertTrue(Validator.validate(new NameHolder(null)).isValid());
        assertTrue(Validator.validate(new AgeHolder(null)).isValid());
        assertTrue(Validator.validate(new EmailHolder(null)).isValid());
    }

    @Test
    void nullFieldProducesOnlyNotNullError() {
        ValidationResult result = Validator.validate(new User(null, null, null));

        assertEquals(Set.of("name:NotNull", "email:NotNull"), keys(result));
        assertEquals(2, result.getErrors().size());
    }

    @Test
    void severalConstraintsOnOneFieldAreAllReported() {
        ValidationResult result = Validator.validate(new MultiViolation());

        assertEquals(2, result.getErrors().size());
        assertEquals(Set.of("value:Size", "value:Email"), keys(result));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "abc", "abcde"})
    void sizeAcceptsBoundaryAndInnerValues(String value) {
        assertTrue(Validator.validate(new NameHolder(value)).isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "abcdef"})
    void sizeRejectsValuesOutsideRange(String value) {
        assertFalse(Validator.validate(new NameHolder(value)).isValid());
    }

    @Test
    void sizeSupportsOtherCharSequences() {
        assertTrue(Validator.validate(new NameHolder(new StringBuilder("abc"))).isValid());
        assertFalse(Validator.validate(new NameHolder(new StringBuilder("a"))).isValid());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 149, 150})
    void rangeAcceptsBoundaryAndInnerValues(int value) {
        assertTrue(Validator.validate(new AgeHolder(value)).isValid());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 151})
    void rangeRejectsValuesOutsideRange(int value) {
        assertFalse(Validator.validate(new AgeHolder(value)).isValid());
    }

    @Test
    void rangeTreatsNaNAsInvalid() {
        ValidationResult result = Validator.validate(new DoubleHolder(Double.NaN));

        assertEquals(Set.of("value:Range"), keys(result));
    }

    @Test
    void rangeRejectsInfiniteValue() {
        assertFalse(Validator.validate(new DoubleHolder(Double.POSITIVE_INFINITY)).isValid());
    }

    @Test
    void primitiveFieldsAreSupported() {
        assertTrue(Validator.validate(new Primitives(5)).isValid());
        assertEquals(Set.of("number:Range"), keys(Validator.validate(new Primitives(11))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com", "a@b.c", "first.last@mail.example.org"})
    void emailAcceptsSimpleValidAddresses(String value) {
        assertTrue(Validator.validate(new EmailHolder(value)).isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "plain", "a@b", "@b.c", "a@", "a b@c.d", "a@b c.d", "a@@b.c"})
    void emailRejectsInvalidAddresses(String value) {
        assertEquals(Set.of("value:Email"), keys(Validator.validate(new EmailHolder(value))));
    }

    @Test
    void validateNullThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Validator.validate(null));
    }

    @Test
    void invalidSizeBoundsCauseConfigurationException() {
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new SizeMinGreaterThanMax()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new SizeNegativeMin()));
    }

    @Test
    void invalidRangeBoundsCauseConfigurationException() {
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new RangeMinGreaterThanMax()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new RangeNaNBound()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new RangeInfiniteBound()));
    }

    @Test
    void annotationOnUnsupportedTypeCausesConfigurationExceptionEvenForNull() {
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new SizeOnInteger()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new RangeOnString()));
        assertThrows(ValidationConfigurationException.class, () -> Validator.validate(new EmailOnInteger()));
    }

    @Test
    void configurationExceptionMentionsClassAndField() {
        ValidationConfigurationException e = assertThrows(
                ValidationConfigurationException.class, () -> Validator.validate(new SizeOnInteger()));

        assertTrue(e.getMessage().contains(SizeOnInteger.class.getName()));
        assertTrue(e.getMessage().contains("value"));
    }

    @Test
    void privateAndInheritedFieldsAreValidated() {
        ValidationResult result = Validator.validate(new Child());

        assertEquals(Set.of("baseField:NotNull", "childField:NotNull"), keys(result));
    }

    @Test
    void staticFieldsAreIgnored() {
        assertTrue(Validator.validate(new WithStatic()).isValid());
    }

    @Test
    void errorsListCannotBeModified() {
        ValidationResult result = Validator.validate(new NotNullHolder(null));

        assertThrows(UnsupportedOperationException.class, () -> result.getErrors().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> result.getErrors().add(new ValidationError("f", "a", "m")));
    }

    @Test
    void resultMakesDefensiveCopy() {
        List<ValidationError> source = new ArrayList<>();
        source.add(new ValidationError("f", "NotNull", "m"));
        ValidationResult result = new ValidationResult(source);

        source.clear();

        assertEquals(1, result.getErrors().size());
        assertFalse(result.isValid());
    }

    @Test
    void resultRejectsNullList() {
        assertThrows(NullPointerException.class, () -> new ValidationResult(null));
    }

    @Test
    void emptyResultIsValid() {
        assertTrue(new ValidationResult(List.of()).isValid());
    }

    @Test
    void validatorDoesNotModifyObject() {
        User user = new User("A", "bad", 200);

        Validator.validate(user);

        assertEquals(3, Validator.validate(user).getErrors().size());
        assertEquals("A", user.name);
        assertEquals("bad", user.email);
        assertEquals(200, user.age);
    }
}
