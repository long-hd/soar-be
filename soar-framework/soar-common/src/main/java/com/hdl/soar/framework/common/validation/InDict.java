package com.hdl.soar.framework.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that the annotated value exists within the given dictionary type.
 *
 * <p>Example usage:
 * <pre>
 * @InDict(type = "sys_common_status")
 * private Integer status;
 * </pre>
 *
 * @see InDictValidator
 * @see InDictCollectionValidator
 */
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PARAMETER,
        ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {InDictValidator.class, InDictCollectionValidator.class})
public @interface InDict {

    /**
     * @return dict type code (e.g. "sys_common_status")
     */
    String type();

    String message() default "Must be within the allowed values {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}