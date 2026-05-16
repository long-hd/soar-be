package com.hdl.soar.framework.common.validation;

import com.hdl.soar.framework.common.core.ArrayValuable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InEnumValidator implements ConstraintValidator<InEnum, Object> {

    private List<?> values;

    @Override
    public void initialize(InEnum annotation) {
        ArrayValuable<?>[] values = annotation.value().getEnumConstants();
        if (values.length == 0) {
            this.values = Collections.emptyList();
        } else {
            this.values = Arrays.asList(values[0].array());
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // If empty, no validation is performed by default, i.e., considered valid
        if (value == null) {
            return true;
        }

        // Validation passed
        if (values.contains(value)) {
            return true;
        }

        // Validation failed, custom error message
        context.disableDefaultConstraintViolation(); // Disable the default message value
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()
                        .replaceAll("\\{value}", value.toString())) // Re-add error message
                .addConstraintViolation();
        return false;
    }

}
