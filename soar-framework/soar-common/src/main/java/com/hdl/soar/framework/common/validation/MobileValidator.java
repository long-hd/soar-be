package com.hdl.soar.framework.common.validation;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.util.validation.ValidationUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MobileValidator implements ConstraintValidator<Mobile, String> {

    @Override
    public void initialize(Mobile annotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // If the phone number is empty, no validation is performed by default, i.e., considered valid
        if (StrUtil.isEmpty(value)) {
            return true;
        }
        // Validate phone number
        return ValidationUtils.isMobile(value);
    }

}
