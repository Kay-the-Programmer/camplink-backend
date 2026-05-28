package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.entity.UserRole;
import com.camplink.repository.UserRepository;
import com.camplink.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only endpoints for user and platform management. All require ADMIN role.")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserRepository userRepo;

    @GetMapping("/users")
    @Operation(
        summary = "List all users",
        description = "Returns every user account on the platform including role, suspension status, and verification state."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User list returned"),
        @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public List<UserResponse> users(@AuthenticationPrincipal UserDetails ud) {
        requireAdmin(ud);
        return adminService.allUsers();
    }

    @PatchMapping("/users/{id}/suspend")
    @Operation(
        summary = "Suspend or unsuspend a user",
        description = "Sets the user's `suspended` flag. A suspended user cannot log in. " +
                      "Request body: `{ \"suspended\": true }` or `{ \"suspended\": false }`."
    )
    @RequestBody(
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(example = "{\"suspended\": true}")
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User suspension status updated"),
        @ApiResponse(responseCode = "403", description = "Not an admin"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserResponse suspend(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "User ID") @PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Boolean> body) {
        requireAdmin(ud);
        return adminService.setSuspended(id, body.getOrDefault("suspended", true));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(
        summary = "Change a user's role",
        description = "Updates the user's role. Valid values: `BUYER`, `SELLER`, `RIDER`, `DRIVER`, `ADMIN`. " +
                      "Request body: `{ \"role\": \"SELLER\" }`."
    )
    @RequestBody(
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(example = "{\"role\": \"SELLER\"}"),
            examples = @ExampleObject(value = "{\"role\": \"SELLER\"}")
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role updated"),
        @ApiResponse(responseCode = "400", description = "Invalid role value"),
        @ApiResponse(responseCode = "403", description = "Not an admin"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserResponse setRole(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "User ID") @PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, String> body) {
        requireAdmin(ud);
        return adminService.setRole(id, body);
    }

    @DeleteMapping("/products/{id}")
    @Operation(
        summary = "Delete any product (admin override)",
        description = "Permanently deletes a product regardless of ownership. " +
                      "Use for moderation — removing illegal or inappropriate listings."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product deleted"),
        @ApiResponse(responseCode = "403", description = "Not an admin"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public void deleteProduct(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Product ID") @PathVariable String id) {
        requireAdmin(ud);
        productService.delete(id, ud.getUsername(), true);
    }

    @GetMapping("/orders")
    @Operation(
        summary = "List all orders (admin view)",
        description = "Returns every order across all sellers, useful for dispute resolution and analytics."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Full order list returned"),
        @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public List<OrderResponse> orders(@AuthenticationPrincipal UserDetails ud) {
        requireAdmin(ud);
        return orderService.all();
    }

    private void requireAdmin(UserDetails ud) {
        boolean admin = userRepo.findById(ud.getUsername())
                .map(u -> u.getRole() == UserRole.ADMIN)
                .orElse(false);
        if (!admin) throw com.camplink.exception.AppException.forbidden("Admin only");
    }
}
