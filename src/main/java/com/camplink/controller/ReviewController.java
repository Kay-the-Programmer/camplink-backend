package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.service.ReviewService;
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
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> create(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(ud.getUsername(), req));
    }

    @GetMapping("/seller/{sellerId}")
    public List<ReviewResponse> bySeller(@PathVariable String sellerId) {
        return reviewService.forSeller(sellerId);
    }

    @GetMapping("/seller/{sellerId}/rating")
    public Map<String, Object> rating(@PathVariable String sellerId) {
        return reviewService.ratingForSeller(sellerId);
    }

    @GetMapping("/order/{orderId}/exists")
    public Map<String, Boolean> orderReviewed(@PathVariable String orderId) {
        return Map.of("reviewed", reviewService.orderReviewed(orderId));
    }
}
