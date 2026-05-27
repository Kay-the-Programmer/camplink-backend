package com.camplink.service;

import com.camplink.dto.UserResponse;
import com.camplink.entity.*;
import com.camplink.exception.AppException;
import com.camplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepo;

    public List<UserResponse> allUsers() {
        return userRepo.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse setSuspended(String userId, boolean suspended) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        user.setSuspended(suspended);
        return UserResponse.from(userRepo.save(user));
    }

    @Transactional
    public UserResponse setRole(String userId, Map<String, String> body) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        String roleStr = body.get("role");
        if (roleStr == null) throw AppException.badRequest("role required");
        user.setRole(UserRole.valueOf(roleStr.toUpperCase()));
        return UserResponse.from(userRepo.save(user));
    }
}
