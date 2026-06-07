package com.camplink.controller;

import com.camplink.dto.ServiceListingRequest;
import com.camplink.dto.ServiceListingResponse;
import com.camplink.service.ServiceListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Services", description = "Student-offered service listings (tutoring, laundry, …)")
public class ServiceController {

    private final ServiceListingService service;

    @GetMapping
    @Operation(summary = "List all service listings",
        description = "Returns every listing, newest first. Clients filter by availability/category.")
    @ApiResponse(responseCode = "200", description = "Listings returned")
    public List<ServiceListingResponse> all() {
        return service.all();
    }

    @GetMapping("/mine")
    @Operation(summary = "List my service listings",
        description = "Returns the authenticated user's own listings, newest first.")
    @ApiResponse(responseCode = "200", description = "Listings returned")
    public List<ServiceListingResponse> mine(@AuthenticationPrincipal UserDetails ud) {
        return service.mine(ud.getUsername());
    }

    @PostMapping
    @Operation(summary = "Create a service listing",
        description = "Advertises a service. Body: title, description, category, optional price/priceNote, available.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Listing created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ServiceListingResponse> create(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody ServiceListingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(ud.getUsername(), req));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a listing's availability",
        description = "Body: `{ \"available\": true }`. Only the owner (or an admin) may update.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listing updated"),
        @ApiResponse(responseCode = "403", description = "Not the owner"),
        @ApiResponse(responseCode = "404", description = "Listing not found")
    })
    public ServiceListingResponse update(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Listing ID") @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        return service.update(id, ud.getUsername(), body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service listing",
        description = "Permanently removes the listing. Only the owner (or an admin) may delete.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Listing deleted"),
        @ApiResponse(responseCode = "403", description = "Not the owner"),
        @ApiResponse(responseCode = "404", description = "Listing not found")
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Listing ID") @PathVariable String id) {
        service.delete(id, ud.getUsername());
        return ResponseEntity.noContent().build();
    }
}
