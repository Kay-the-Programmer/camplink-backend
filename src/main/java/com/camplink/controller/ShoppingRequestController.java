package com.camplink.controller;

import com.camplink.dto.CreateShoppingRequestRequest;
import com.camplink.dto.ShoppingRequestResponse;
import com.camplink.service.ShoppingRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class ShoppingRequestController {

    private final ShoppingRequestService service;

    @GetMapping
    public List<ShoppingRequestResponse> open() {
        return service.getOpen();
    }

    @GetMapping("/mine")
    public List<ShoppingRequestResponse> mine(@AuthenticationPrincipal UserDetails ud) {
        return service.getMine(ud.getUsername());
    }

    @GetMapping("/running")
    public List<ShoppingRequestResponse> running(@AuthenticationPrincipal UserDetails ud) {
        return service.getRunning(ud.getUsername());
    }

    @PostMapping
    public ResponseEntity<ShoppingRequestResponse> create(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody CreateShoppingRequestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(ud.getUsername(), req));
    }

    @PostMapping("/{id}/accept")
    public ShoppingRequestResponse accept(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id) {
        return service.accept(id, ud.getUsername());
    }

    @PostMapping("/{id}/fulfill")
    public ShoppingRequestResponse fulfill(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id) {
        return service.fulfill(id, ud.getUsername());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id) {
        service.cancel(id, ud.getUsername());
        return ResponseEntity.noContent().build();
    }
}
