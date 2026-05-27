package com.camplink.service;

import com.camplink.dto.*;
import com.camplink.entity.*;
import com.camplink.exception.AppException;
import com.camplink.repository.UserRepository;
import com.camplink.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw AppException.conflict("Email already in use");
        }
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(req.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName().trim())
                .phone(req.getPhone())
                .studentId(req.getStudentId())
                .role(req.getRole() == UserRole.ADMIN ? UserRole.BUYER : req.getRole()) // no self-admin
                .suspended(false)
                .build();
        userRepo.save(user);
        String token = jwtUtil.generate(user.getId());
        return new AuthResponse(token, UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> AppException.badRequest("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw AppException.badRequest("Invalid email or password");
        }
        if (user.isSuspended()) throw AppException.forbidden("Account suspended");
        String token = jwtUtil.generate(user.getId());
        return new AuthResponse(token, UserResponse.from(user));
    }

    @Transactional
    public UserResponse updateProfile(String userId, ProfileUpdateRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        if (req.getFullName()  != null) user.setFullName(req.getFullName());
        if (req.getPhone()     != null) user.setPhone(req.getPhone());
        if (req.getStudentId() != null) user.setStudentId(req.getStudentId());
        if (req.getPhotoUrl()  != null) user.setPhotoUrl(req.getPhotoUrl());
        if (req.getHostel()    != null) user.setHostel(req.getHostel());
        if (req.getLocation()  != null) user.setLocation(req.getLocation());
        return UserResponse.from(userRepo.save(user));
    }
}
