package validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Числовое значение должно лежать в замкнутом диапазоне [min, max].
 * Значение null пропускается, NaN считается невалидным.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {
    double min() default -Double.MAX_VALUE;

    double max() default Double.MAX_VALUE;

    String message() default "Value is out of allowed range";
}
