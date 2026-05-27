package com.camplink.dto;

import com.camplink.entity.User;
import com.camplink.entity.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String studentId;
    private UserRole role;
    private String photoUrl;
    private String hostel;
    private String location;
    private boolean suspended;
    private LocalDateTime createdAt;

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.id        = u.getId();
        r.email     = u.getEmail();
        r.fullName  = u.getFullName();
        r.phone     = u.getPhone();
        r.studentId = u.getStudentId();
        r.role      = u.getRole();
        r.photoUrl  = u.getPhotoUrl();
        r.hostel    = u.getHostel();
        r.location  = u.getLocation();
        r.suspended = u.isSuspended();
        r.createdAt = u.getCreatedAt();
        return r;
    }
}
