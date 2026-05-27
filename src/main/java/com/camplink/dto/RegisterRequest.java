package com.camplink.dto;

import com.camplink.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email(message = "Valid email required")
    @NotBlank
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank
    private String password;

    @NotBlank(message = "Full name required")
    private String fullName;

    private String phone;
    private String studentId;

    @NotNull(message = "Role required (BUYER or SELLER)")
    private UserRole role;
}
