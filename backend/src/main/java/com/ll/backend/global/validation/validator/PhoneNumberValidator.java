package com.ll.backend.global.validation.validator;

import com.ll.backend.global.validation.annotation.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    private static final String PHONE_REGEX = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}

