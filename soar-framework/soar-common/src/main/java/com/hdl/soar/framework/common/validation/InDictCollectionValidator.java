package com.hdl.soar.framework.common.validation;

import com.hdl.soar.framework.common.biz.system.dict.util.DictFrameworkUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InDictCollectionValidator implements ConstraintValidator<InDict, Collection<?>> {

    private String dictType;

    @Override
    public void initialize(InDict annotation) {
        this.dictType = annotation.type();
    }

    @Override
    public boolean isValid(Collection<?> list, ConstraintValidatorContext context) {
        if (list == null || list.isEmpty()) {
            return true;
        }

        List<String> values = DictFrameworkUtils.getDictDataValueList(dictType);
        boolean allValid = list.stream()
                .allMatch(item -> values.stream()
                        .anyMatch(v -> v.equalsIgnoreCase(item.toString())));
        if(allValid){
            return true;
        }

        // Validation failed
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                context.getDefaultConstraintMessageTemplate()
                        .replaceAll("\\{value}", list.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(",")))
        ).addConstraintViolation();
        return false;
    }

}
