package com.camplink.controller;

import com.camplink.dto.RideBookingRequest;
import com.camplink.dto.RideBookingResponse;
import com.camplink.service.RideService;
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

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Rides", description = "Request, accept and manage campus rides")
public class RideController {

    private final RideService rideService;

    @GetMapping
    @Operation(
        summary = "List pending rides",
        description = "Returns all rides awaiting a driver. Pass `?status=pending` (the only " +
                      "supported filter). Clients exclude their own rides from the driver view."
    )
    @ApiResponse(responseCode = "200", description = "Pending rides returned")
    public List<RideBookingResponse> pending(
            @Parameter(description = "Only `pending` is supported")
            @RequestParam(required = false) String status) {
        return rideService.pending();
    }

    @GetMapping("/mine")
    @Operation(
        summary = "List my rides",
        description = "Returns rides where the authenticated user is the passenger or the driver, newest first."
    )
    @ApiResponse(responseCode = "200", description = "Rides returned")
    public List<RideBookingResponse> mine(@AuthenticationPrincipal UserDetails ud) {
        return rideService.mine(ud.getUsername());
    }

    @PostMapping
    @Operation(
        summary = "Book a ride",
        description = "Creates a PENDING ride. direction: `CAMPUS_TO_TOWN` | `TOWN_TO_CAMPUS`; " +
                      "dropOff: `ACROSS` | `INSIDE_CAMPUS`; type: `INSTANT` | `SCHEDULED` " +
                      "(scheduledAt required when SCHEDULED). Fare is computed from the admin ride prices."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ride booked"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<RideBookingResponse> create(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody RideBookingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rideService.create(ud.getUsername(), req));
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept a ride (driver)",
        description = "Assigns the authenticated driver and moves the ride to ACCEPTED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ride accepted"),
        @ApiResponse(responseCode = "400", description = "Ride no longer available or is your own"),
        @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public RideBookingResponse accept(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Ride ID") @PathVariable String id) {
        return rideService.accept(id, ud.getUsername());
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete a ride (driver)",
        description = "Marks an accepted ride as COMPLETED. Only the assigned driver may call this.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ride completed"),
        @ApiResponse(responseCode = "403", description = "Not the assigned driver"),
        @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public RideBookingResponse complete(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Ride ID") @PathVariable String id) {
        return rideService.complete(id, ud.getUsername());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a ride",
        description = "Marks the ride CANCELLED. Either the passenger or the assigned driver may call this.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ride cancelled"),
        @ApiResponse(responseCode = "403", description = "Not a participant in this ride"),
        @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public RideBookingResponse cancel(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Ride ID") @PathVariable String id) {
        return rideService.cancel(id, ud.getUsername());
    }
}
