package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.entity.UserRole;
import com.camplink.repository.UserRepository;
import com.camplink.service.ProductService;
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

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Browse and manage marketplace listings")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepo;

    @GetMapping
    @SecurityRequirements   // public
    @Operation(
        summary = "List all available products",
        description = "Returns every product that is currently marked as available. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Product list returned")
    public List<ProductResponse> all() {
        return productService.getAll();
    }

    @GetMapping("/my")
    @Operation(
        summary = "List my own products (seller)",
        description = "Returns all products belonging to the authenticated seller, including unavailable ones."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product list returned"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT")
    })
    public List<ProductResponse> my(@AuthenticationPrincipal UserDetails ud) {
        return productService.getBySeller(ud.getUsername());
    }

    @GetMapping("/seller/{sellerId}")
    @SecurityRequirements   // public
    @Operation(
        summary = "List a specific seller's products",
        description = "Returns all available products for the given seller ID. No authentication required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product list returned"),
        @ApiResponse(responseCode = "404", description = "Seller not found")
    })
    public List<ProductResponse> bySeller(
            @Parameter(description = "Seller user ID") @PathVariable String sellerId) {
        return productService.getBySeller(sellerId);
    }

    @GetMapping("/{id}")
    @SecurityRequirements   // public
    @Operation(
        summary = "Get a single product by ID",
        description = "Returns full product details. No authentication required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product returned"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductResponse get(
            @Parameter(description = "Product ID") @PathVariable String id) {
        return productService.getById(id);
    }

    @PostMapping
    @Operation(
        summary = "Create a new product listing",
        description = "Creates a product under the authenticated seller's account. Role must be SELLER or ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT"),
        @ApiResponse(responseCode = "403", description = "Not a seller")
    })
    public ResponseEntity<ProductResponse> create(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody ProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(ud.getUsername(), req));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update a product",
        description = "Replaces all editable fields of the product. Caller must be the owning seller or an admin."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated"),
        @ApiResponse(responseCode = "403", description = "Not the owner or admin"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductResponse update(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Product ID") @PathVariable String id,
            @Valid @RequestBody ProductRequest req) {
        return productService.update(id, ud.getUsername(), req);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a product",
        description = "Permanently deletes the product. Caller must be the owning seller or an admin."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted"),
        @ApiResponse(responseCode = "403", description = "Not the owner or admin"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Product ID") @PathVariable String id) {
        boolean isAdmin = userRepo.findById(ud.getUsername())
                .map(u -> u.getRole() == UserRole.ADMIN)
                .orElse(false);
        productService.delete(id, ud.getUsername(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
