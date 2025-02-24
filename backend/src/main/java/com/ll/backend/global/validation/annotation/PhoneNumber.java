package com.ll.backend.global.validation.annotation;

import com.ll.backend.global.validation.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@NotBlank
public @interface PhoneNumber {
    String message() default "유효하지 않은 휴대폰 번호 형식입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

