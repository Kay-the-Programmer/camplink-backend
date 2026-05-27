package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.entity.UserRole;
import com.camplink.repository.UserRepository;
import com.camplink.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserRepository userRepo;

    @GetMapping("/users")
    public List<UserResponse> users(@AuthenticationPrincipal UserDetails ud) {
        requireAdmin(ud);
        return adminService.allUsers();
    }

    @PatchMapping("/users/{id}/suspend")
    public UserResponse suspend(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        requireAdmin(ud);
        return adminService.setSuspended(id, body.getOrDefault("suspended", true));
    }

    @PatchMapping("/users/{id}/role")
    public UserResponse setRole(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        requireAdmin(ud);
        return adminService.setRole(id, body);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id) {
        requireAdmin(ud);
        productService.delete(id, ud.getUsername(), true);
    }

    @GetMapping("/orders")
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
