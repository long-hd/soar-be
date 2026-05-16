package com.hdl.soar.framework.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

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
@Constraint(
        validatedBy = TelephoneValidator.class
)
public @interface Telephone {

    String message() default "Invalid phone number format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
