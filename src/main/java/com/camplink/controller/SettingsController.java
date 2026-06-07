package com.camplink.controller;

import com.camplink.dto.RidePricesDto;
import com.camplink.entity.UserRole;
import com.camplink.repository.UserRepository;
import com.camplink.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Platform configuration. Reads are public; writes require ADMIN.")
public class SettingsController {

    private final SettingsService settingsService;
    private final UserRepository userRepo;

    @GetMapping("/ride-prices")
    @SecurityRequirements   // public — students see fares before booking
    @Operation(
        summary = "Get current ride fares",
        description = "Returns the per-seat fares shown to students on the booking screen."
    )
    @ApiResponse(responseCode = "200", description = "Current fares returned")
    public RidePricesDto getRidePrices() {
        return settingsService.getRidePrices();
    }

    @PutMapping("/ride-prices")
    @Operation(
        summary = "Update ride fares (admin only)",
        description = "Sets the per-seat fares. Body: " +
                      "`{ \"campusToTown\": 20, \"townToAcross\": 15, \"townToInsideCampus\": 25 }`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fares updated"),
        @ApiResponse(responseCode = "400", description = "A fare is missing or not greater than 0"),
        @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public RidePricesDto updateRidePrices(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody RidePricesDto dto) {
        requireAdmin(ud);
        return settingsService.updateRidePrices(dto);
    }

    private void requireAdmin(UserDetails ud) {
        boolean admin = userRepo.findById(ud.getUsername())
                .map(u -> u.getRole() == UserRole.ADMIN)
                .orElse(false);
        if (!admin) throw com.camplink.exception.AppException.forbidden("Admin only");
    }
}
