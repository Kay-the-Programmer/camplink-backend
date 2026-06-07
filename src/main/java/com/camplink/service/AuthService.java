package com.camplink.service;

import com.camplink.dto.*;
import com.camplink.entity.*;
import com.camplink.exception.AppException;
import com.camplink.repository.UserRepository;
import com.camplink.security.GoogleTokenVerifier;
import com.camplink.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
    private final GoogleTokenVerifier googleVerifier;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw AppException.conflict("Email already in use");
        }
        // No self-admin; providers start life PENDING and await admin review.
        UserRole role = req.getRole() == UserRole.ADMIN ? UserRole.BUYER : req.getRole();
        boolean needsVehicle = role.requiresVehicleInfo();
        if (needsVehicle) {
            requireVehicleInfo(req.getVehicleName(), req.getPlateNumber(), req.getNrcNumber());
        }
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(req.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName().trim())
                .phone(req.getPhone())
                .studentId(req.getStudentId())
                .role(role)
                .verificationStatus(role.isProvider() ? VerificationStatus.PENDING : null)
                .vehicleName(needsVehicle ? req.getVehicleName().trim() : null)
                .plateNumber(needsVehicle ? req.getPlateNumber().trim() : null)
                .nrcNumber(needsVehicle ? req.getNrcNumber().trim() : null)
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

    /**
     * Verifies a Google ID token, then finds or creates the matching user.
     * Brand-new accounts (and any without a phone) are flagged
     * {@code needsProfileCompletion} so the app routes them to fill in the rest.
     */
    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        GoogleIdToken.Payload payload = googleVerifier.verify(idToken);

        if (Boolean.FALSE.equals(payload.getEmailVerified())) {
            throw AppException.badRequest("Google email is not verified");
        }
        String email = payload.getEmail();
        if (email == null || email.isBlank()) {
            throw AppException.badRequest("Google account has no email");
        }
        email = email.toLowerCase().trim();
        String name    = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            user = User.builder()
                    .id(UUID.randomUUID().toString())
                    .email(email)
                    // Unusable random password: Google users authenticate via
                    // OAuth, but passwordHash is non-null in the schema.
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .fullName(name != null && !name.isBlank() ? name.trim() : email)
                    .photoUrl(picture)
                    .role(UserRole.BUYER)
                    .suspended(false)
                    .build();
            userRepo.save(user);
        } else if (user.getPhotoUrl() == null && picture != null) {
            user.setPhotoUrl(picture);
            userRepo.save(user);
        }

        if (user.isSuspended()) throw AppException.forbidden("Account suspended");

        boolean needsProfile = user.getPhone() == null || user.getPhone().isBlank();
        String token = jwtUtil.generate(user.getId());
        return new AuthResponse(token, UserResponse.from(user), needsProfile);
    }

    /**
     * Fills in the details Google can't provide (phone, role, student ID) for a
     * freshly signed-up user, then returns a fresh token + the updated user.
     */
    @Transactional
    public AuthResponse completeGoogleProfile(String userId, CompleteProfileRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getPhone() != null) user.setPhone(req.getPhone().trim());
        if (req.getStudentId() != null) user.setStudentId(req.getStudentId().trim());
        if (req.getRole() != null) {
            UserRole role = req.getRole() == UserRole.ADMIN ? UserRole.BUYER : req.getRole();
            user.setRole(role);
            applyVehicleInfo(user, role,
                    req.getVehicleName(), req.getPlateNumber(), req.getNrcNumber());
            // A Google user picking a provider role here is self-service, so they
            // enter the review queue just like a password sign-up would.
            if (role.isProvider()) {
                if (user.getVerificationStatus() == null) {
                    user.setVerificationStatus(VerificationStatus.PENDING);
                }
            } else {
                user.setVerificationStatus(null);
                user.setRejectionReason(null);
            }
        }
        userRepo.save(user);

        String token = jwtUtil.generate(user.getId());
        return new AuthResponse(token, UserResponse.from(user), false);
    }

    /// A buyer asks to become a provider. The role switches to the requested
    /// provider role and the account enters the PENDING review queue, where an
    /// admin approves or rejects it via the existing verification flow.
    @Transactional
    public UserResponse requestUpgrade(String userId, java.util.Map<String, String> body) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        if (user.getRole() != UserRole.BUYER) {
            throw AppException.badRequest("Only buyer accounts can request an upgrade");
        }
        String roleStr = body.get("role");
        if (roleStr == null) throw AppException.badRequest("role required");
        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Invalid role: " + roleStr);
        }
        if (!role.isProvider()) {
            throw AppException.badRequest("Choose a seller, rider or driver role");
        }
        user.setRole(role);
        applyVehicleInfo(user, role,
                body.get("vehicleName"), body.get("plateNumber"), body.get("nrcNumber"));
        user.setVerificationStatus(VerificationStatus.PENDING);
        user.setRejectionReason(null);
        return UserResponse.from(userRepo.save(user));
    }

    // ── Vehicle / NRC helpers ─────────────────────────────────────────────────

    /// Sets vehicle/NRC details for riders & drivers (required), or clears them
    /// for roles that don't operate a vehicle.
    private void applyVehicleInfo(User user, UserRole role,
                                  String vehicleName, String plateNumber, String nrcNumber) {
        if (role.requiresVehicleInfo()) {
            requireVehicleInfo(vehicleName, plateNumber, nrcNumber);
            user.setVehicleName(vehicleName.trim());
            user.setPlateNumber(plateNumber.trim());
            user.setNrcNumber(nrcNumber.trim());
        } else {
            user.setVehicleName(null);
            user.setPlateNumber(null);
            user.setNrcNumber(null);
        }
    }

    private void requireVehicleInfo(String vehicleName, String plateNumber, String nrcNumber) {
        if (isBlank(vehicleName) || isBlank(plateNumber) || isBlank(nrcNumber)) {
            throw AppException.badRequest(
                    "Vehicle name, number plate and NRC number are required for riders and drivers");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
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
