package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.entity.UserRole;
import com.camplink.repository.UserRepository;
import com.camplink.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepo;

    @PostMapping
    public ResponseEntity<OrderResponse> place(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody OrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.place(ud.getUsername(), req));
    }

    @GetMapping("/buyer")
    public List<OrderResponse> myOrders(@AuthenticationPrincipal UserDetails ud) {
        return orderService.forBuyer(ud.getUsername());
    }

    @GetMapping("/seller")
    public List<OrderResponse> sellerOrders(@AuthenticationPrincipal UserDetails ud) {
        return orderService.forSeller(ud.getUsername());
    }

    @GetMapping
    public List<OrderResponse> all(@AuthenticationPrincipal UserDetails ud) {
        requireAdmin(ud);
        return orderService.all();
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequest req) {
        return orderService.updateStatus(id, ud.getUsername(), req);
    }

    @PatchMapping("/{id}/paid")
    public OrderResponse markPaid(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id) {
        return orderService.markPaid(id, ud.getUsername());
    }

    private void requireAdmin(UserDetails ud) {
        boolean admin = userRepo.findById(ud.getUsername())
                .map(u -> u.getRole() == UserRole.ADMIN)
                .orElse(false);
        if (!admin) throw com.camplink.exception.AppException.forbidden("Admin only");
    }
}
