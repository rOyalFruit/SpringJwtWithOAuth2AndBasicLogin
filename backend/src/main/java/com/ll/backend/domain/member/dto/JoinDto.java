package com.ll.backend.domain.member.dto;

import com.ll.backend.global.validation.annotation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record JoinDto(
        @NotBlank @Length(min = 3, max = 10) String username, // User's unique identifier
        @NotBlank @Length(min = 8) String password,           // User's password
        @NotBlank @Length(min = 8) String confirmPassword,    // Must match the password
        @NotBlank @Length(max = 10) String nickname,          // User's display name
        @NotBlank @Email String email,                        // User's email address, must be valid
        @NotBlank @PhoneNumber String phone                   // User's phone number, must be valid
        ) {}
