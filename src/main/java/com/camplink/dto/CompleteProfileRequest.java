package com.camplink.dto;

import com.camplink.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Sent by Google-authenticated users to fill in the details Google can't
 * provide (phone, chosen role, student ID) right after first sign-in.
 */
@Data
public class CompleteProfileRequest {
    @NotBlank(message = "Phone required")
    private String phone;

    @NotNull(message = "Role required")
    private UserRole role;

    private String studentId;
    private String fullName;

    // Required when role is RIDER or DRIVER (validated in the service).
    private String vehicleName;
    private String plateNumber;
    private String nrcNumber;
}
