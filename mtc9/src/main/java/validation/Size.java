package validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Длина значения типа {@link CharSequence} должна лежать в замкнутом диапазоне [min, max].
 * Значение null пропускается.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Size {
    int min() default 0;

    int max() default Integer.MAX_VALUE;

    String message() default "Size is out of allowed range";
}
