package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.entity.UserRole;
import com.camplink.repository.UserRepository;
import com.camplink.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Orders", description = "Place and track marketplace orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepo;

    @PostMapping
    @Operation(
        summary = "Place a new order",
        description = "Buyer submits a cart as an order. All items must belong to the same seller. " +
                      "Accepted delivery methods: `DELIVERY`, `PICKUP`. " +
                      "Accepted payment methods: `CASH_ON_DELIVERY`, `MTN_MOMO`, `AIRTEL_MONEY`, `ZAMTEL_KWACHA`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order placed, status = PENDING"),
        @ApiResponse(responseCode = "400", description = "Validation error or mixed sellers in cart"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT")
    })
    public ResponseEntity<OrderResponse> place(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody OrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.place(ud.getUsername(), req));
    }

    @GetMapping("/buyer")
    @Operation(
        summary = "List my orders (buyer view)",
        description = "Returns all orders placed by the authenticated buyer, newest first."
    )
    @ApiResponse(responseCode = "200", description = "Order list returned")
    public List<OrderResponse> myOrders(@AuthenticationPrincipal UserDetails ud) {
        return orderService.forBuyer(ud.getUsername());
    }

    @GetMapping("/seller")
    @Operation(
        summary = "List incoming orders (seller view)",
        description = "Returns all orders directed at the authenticated seller's products, newest first."
    )
    @ApiResponse(responseCode = "200", description = "Order list returned")
    public List<OrderResponse> sellerOrders(@AuthenticationPrincipal UserDetails ud) {
        return orderService.forSeller(ud.getUsername());
    }

    @GetMapping
    @Operation(
        summary = "List all orders (admin only)",
        description = "Returns every order on the platform. Requires ADMIN role."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Full order list returned"),
        @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public List<OrderResponse> all(@AuthenticationPrincipal UserDetails ud) {
        requireAdmin(ud);
        return orderService.all();
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update an order's status",
        description = "Sellers move orders through the lifecycle: `PENDING` → `CONFIRMED` → `DELIVERED`. " +
                      "Either party can set `CANCELLED`. Valid statuses: `PENDING`, `CONFIRMED`, `DELIVERED`, `CANCELLED`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "403", description = "Not the seller of this order"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public OrderResponse updateStatus(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Order ID") @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequest req) {
        return orderService.updateStatus(id, ud.getUsername(), req);
    }

    @PatchMapping("/{id}/paid")
    @Operation(
        summary = "Mark an order as paid",
        description = "Seller confirms that payment has been received. Sets paymentStatus to `PAID`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order marked as paid"),
        @ApiResponse(responseCode = "403", description = "Not the seller of this order"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public OrderResponse markPaid(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Order ID") @PathVariable String id) {
        return orderService.markPaid(id, ud.getUsername());
    }

    private void requireAdmin(UserDetails ud) {
        boolean admin = userRepo.findById(ud.getUsername())
                .map(u -> u.getRole() == UserRole.ADMIN)
                .orElse(false);
        if (!admin) throw com.camplink.exception.AppException.forbidden("Admin only");
    }
}
