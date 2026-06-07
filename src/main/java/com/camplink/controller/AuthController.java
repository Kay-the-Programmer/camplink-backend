package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.exception.AppException;
import com.camplink.repository.UserRepository;
import com.camplink.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, log in, and manage your own profile")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepo;

    @PostMapping("/register")
    @SecurityRequirements   // public — no JWT required
    @Operation(
        summary = "Register a new account",
        description = "Creates a user account with the given role (BUYER, SELLER, RIDER, DRIVER). " +
                      "Returns a JWT token valid for 7 days plus the newly created user object."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created — token and user returned"),
        @ApiResponse(responseCode = "400", description = "Validation error or email already taken"),
        @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    @SecurityRequirements   // public — no JWT required
    @Operation(
        summary = "Login with email and password",
        description = "Authenticates credentials and returns a fresh JWT token."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful — token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/google")
    @SecurityRequirements   // public — no JWT required
    @Operation(
        summary = "Login or sign up with Google",
        description = "Verifies a Google ID token (signature, expiry, issuer and audience), " +
                      "then finds or creates the matching user. Returns a JWT plus the user. " +
                      "When the account still lacks a phone/role, needsProfileCompletion is true " +
                      "and the client should follow up with POST /auth/complete-profile."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated — token and user returned"),
        @ApiResponse(responseCode = "400", description = "Missing, invalid, or wrong-audience token"),
        @ApiResponse(responseCode = "403", description = "Account suspended")
    })
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest req) {
        return ResponseEntity.ok(authService.loginWithGoogle(req.getIdToken()));
    }

    @PostMapping("/complete-profile")
    @Operation(
        summary = "Complete profile after Google sign-up",
        description = "Sets phone, role and (optionally) student ID / full name for the " +
                      "currently authenticated user, then returns a fresh token and the user."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile completed — token and user returned"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT")
    })
    public ResponseEntity<AuthResponse> completeProfile(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody CompleteProfileRequest req) {
        if (ud == null) throw AppException.unauthorized("Authentication required");
        return ResponseEntity.ok(authService.completeGoogleProfile(ud.getUsername(), req));
    }

    @PostMapping("/upgrade-request")
    @Operation(
        summary = "Request a provider account upgrade",
        description = "A buyer requests to become a seller / rider / driver. " +
                      "Body: `{ \"role\": \"SELLER\" }`. The account moves to PENDING " +
                      "verification for an admin to approve. Returns the updated user."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Upgrade requested — account pending review"),
        @ApiResponse(responseCode = "400", description = "Not a buyer, or invalid role"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT")
    })
    public ResponseEntity<UserResponse> upgradeRequest(
            @AuthenticationPrincipal UserDetails ud,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body) {
        if (ud == null) throw AppException.unauthorized("Authentication required");
        return ResponseEntity.ok(authService.requestUpgrade(ud.getUsername(), body));
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get my profile",
        description = "Returns the full profile of the currently authenticated user."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT")
    })
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(
                UserResponse.from(userRepo.findById(ud.getUsername())
                        .orElseThrow()));
    }

    @PutMapping("/me")
    @Operation(
        summary = "Update my profile",
        description = "Updates editable fields: fullName, phone, studentId, photoUrl, hostel, location. " +
                      "Fields omitted from the request body are left unchanged."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT")
    })
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody ProfileUpdateRequest req) {
        return ResponseEntity.ok(authService.updateProfile(ud.getUsername(), req));
    }
}
