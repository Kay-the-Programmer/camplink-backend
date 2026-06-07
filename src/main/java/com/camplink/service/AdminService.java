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
        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Invalid role: " + roleStr);
        }
        user.setRole(role);
        // Keep verification consistent with the new role. An admin granting a
        // provider role is an act of trust, so the account is approved outright;
        // moving back to buyer/admin clears any verification state.
        if (role.isProvider()) {
            user.setVerificationStatus(VerificationStatus.APPROVED);
            user.setRejectionReason(null);
        } else {
            user.setVerificationStatus(null);
            user.setRejectionReason(null);
        }
        return UserResponse.from(userRepo.save(user));
    }

    @Transactional
    public UserResponse verifyProvider(String userId, Map<String, String> body) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        if (!user.getRole().isProvider()) {
            throw AppException.badRequest("Only provider accounts can be verified");
        }
        String statusStr = body.get("verificationStatus");
        if (statusStr == null) throw AppException.badRequest("verificationStatus required");
        VerificationStatus status;
        try {
            status = VerificationStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Invalid verificationStatus: " + statusStr);
        }
        user.setVerificationStatus(status);
        // Only a rejection carries a reason; clear it otherwise.
        user.setRejectionReason(
                status == VerificationStatus.REJECTED ? body.get("rejectionReason") : null);
        return UserResponse.from(userRepo.save(user));
    }
}
