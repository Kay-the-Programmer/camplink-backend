package com.camplink.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private String studentId;
    private String photoUrl;
    private String hostel;
    private String location;
}
