package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Seller ratings and buyer reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(
        summary = "Submit a review for a seller",
        description = "Creates a review (rating 1–5 + optional comment) for a seller. " +
                      "The buyer must have a delivered order from that seller. " +
                      "Only one review per order is allowed."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Review submitted"),
        @ApiResponse(responseCode = "400", description = "Rating out of range or order already reviewed"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT"),
        @ApiResponse(responseCode = "404", description = "Seller or order not found")
    })
    public ResponseEntity<ReviewResponse> create(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(ud.getUsername(), req));
    }

    @GetMapping("/seller/{sellerId}")
    @SecurityRequirements   // public
    @Operation(
        summary = "List all reviews for a seller",
        description = "Returns all reviews written about a seller, newest first. No authentication required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review list returned"),
        @ApiResponse(responseCode = "404", description = "Seller not found")
    })
    public List<ReviewResponse> bySeller(
            @Parameter(description = "Seller user ID") @PathVariable String sellerId) {
        return reviewService.forSeller(sellerId);
    }

    @GetMapping("/seller/{sellerId}/rating")
    @SecurityRequirements   // public
    @Operation(
        summary = "Get a seller's aggregate rating",
        description = "Returns `{ \"average\": 4.5, \"count\": 12 }` — the mean star rating and total review count. " +
                      "No authentication required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rating summary returned"),
        @ApiResponse(responseCode = "404", description = "Seller not found")
    })
    public Map<String, Object> rating(
            @Parameter(description = "Seller user ID") @PathVariable String sellerId) {
        return reviewService.ratingForSeller(sellerId);
    }

    @GetMapping("/order/{orderId}/exists")
    @Operation(
        summary = "Check if an order has been reviewed",
        description = "Returns `{ \"reviewed\": true/false }`. " +
                      "Used by the UI to hide the 'Leave a review' button once a review exists."
    )
    @ApiResponse(responseCode = "200", description = "Review existence flag returned")
    public Map<String, Boolean> orderReviewed(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        return Map.of("reviewed", reviewService.orderReviewed(orderId));
    }
}
