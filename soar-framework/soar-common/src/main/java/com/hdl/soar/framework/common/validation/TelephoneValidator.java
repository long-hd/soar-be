package com.hdl.soar.framework.common.validation;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PhoneUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelephoneValidator implements ConstraintValidator<Telephone, String> {

    @Override
    public void initialize(Telephone annotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // If the phone number is empty, no validation is performed by default, i.e., considered valid
        if(CharSequenceUtil.isEmpty(value)){
            return true;
        }

        // Validate phone number
        return PhoneUtil.isTel(value) || PhoneUtil.isPhone(value);
    }
}
