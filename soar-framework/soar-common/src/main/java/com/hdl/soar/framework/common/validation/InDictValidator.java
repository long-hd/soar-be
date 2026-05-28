package com.hdl.soar.framework.common.validation;

import com.hdl.soar.framework.common.biz.system.dict.util.DictFrameworkUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class InDictValidator implements ConstraintValidator<InDict, Object> {

    private String dictType;

    @Override
    public void initialize(InDict annotation) {
        this.dictType = annotation.type();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Null is considered valid — use @NotNull to enforce non-null
        if (value == null) {
            return true;
        }

        List<String> values = DictFrameworkUtils.getDictDataValueList(dictType);
        if (values.stream().anyMatch(v -> v.equalsIgnoreCase(value.toString()))) {
            return true;
        }

        // Validation failed — custom message showing allowed values
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                context.getDefaultConstraintMessageTemplate()
                        .replaceAll("\\{value}", values.toString())
        ).addConstraintViolation();
        return false;
    }

}
