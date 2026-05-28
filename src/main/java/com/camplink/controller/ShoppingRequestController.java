package com.camplink.controller;

import com.camplink.dto.CreateShoppingRequestRequest;
import com.camplink.dto.ShoppingRequestResponse;
import com.camplink.service.ShoppingRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Shopping Requests", description = "Campus errand / runner requests — buyers post, riders fulfil")
public class ShoppingRequestController {

    private final ShoppingRequestService service;

    @GetMapping
    @SecurityRequirements   // public — riders browse without logging in first
    @Operation(
        summary = "List all open shopping requests",
        description = "Returns every request with status `OPEN` that riders can accept. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Open request list returned")
    public List<ShoppingRequestResponse> open() {
        return service.getOpen();
    }

    @GetMapping("/mine")
    @Operation(
        summary = "List requests I created",
        description = "Returns all shopping requests posted by the authenticated buyer, across all statuses."
    )
    @ApiResponse(responseCode = "200", description = "Request list returned")
    public List<ShoppingRequestResponse> mine(@AuthenticationPrincipal UserDetails ud) {
        return service.getMine(ud.getUsername());
    }

    @GetMapping("/running")
    @Operation(
        summary = "List requests I am currently running",
        description = "Returns requests that the authenticated rider has accepted but not yet fulfilled. " +
                      "Status is `ACCEPTED`."
    )
    @ApiResponse(responseCode = "200", description = "Running request list returned")
    public List<ShoppingRequestResponse> running(@AuthenticationPrincipal UserDetails ud) {
        return service.getRunning(ud.getUsername());
    }

    @PostMapping
    @Operation(
        summary = "Create a new shopping request",
        description = "Buyer posts a list of items to be fetched from a shop. " +
                      "Include `deliveryHostel`, optional `deliveryRoom`, optional `budget`, " +
                      "`runnerFee` (amount the buyer pays the rider), and an `items` array with " +
                      "each item's name, quantity, estimated price, and optional notes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Request created with status OPEN"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT")
    })
    public ResponseEntity<ShoppingRequestResponse> create(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody CreateShoppingRequestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(ud.getUsername(), req));
    }

    @PostMapping("/{id}/accept")
    @Operation(
        summary = "Accept a shopping request (rider)",
        description = "Rider claims the request, setting its status to `ACCEPTED` and recording their user ID. " +
                      "Only one rider can accept a given request."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request accepted"),
        @ApiResponse(responseCode = "400", description = "Request is no longer open"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ShoppingRequestResponse accept(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Shopping request ID") @PathVariable String id) {
        return service.accept(id, ud.getUsername());
    }

    @PostMapping("/{id}/fulfill")
    @Operation(
        summary = "Fulfil a shopping request (rider)",
        description = "Rider marks the request as delivered. Status changes to `FULFILLED`. " +
                      "Only the assigned rider can call this endpoint."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request marked as fulfilled"),
        @ApiResponse(responseCode = "403", description = "Not the assigned rider"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ShoppingRequestResponse fulfill(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Shopping request ID") @PathVariable String id) {
        return service.fulfill(id, ud.getUsername());
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancel my shopping request",
        description = "Buyer cancels their own open request. Only works while status is `OPEN`. " +
                      "Returns `204 No Content`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Request cancelled"),
        @ApiResponse(responseCode = "400", description = "Request has already been accepted and cannot be cancelled"),
        @ApiResponse(responseCode = "403", description = "Not the requester"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Shopping request ID") @PathVariable String id) {
        service.cancel(id, ud.getUsername());
        return ResponseEntity.noContent().build();
    }
}
