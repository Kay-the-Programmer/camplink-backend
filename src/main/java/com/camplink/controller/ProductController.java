package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.entity.UserRole;
import com.camplink.repository.UserRepository;
import com.camplink.service.ProductService;
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
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepo;

    @GetMapping
    public List<ProductResponse> all() {
        return productService.getAll();
    }

    @GetMapping("/my")
    public List<ProductResponse> my(@AuthenticationPrincipal UserDetails ud) {
        return productService.getBySeller(ud.getUsername());
    }

    @GetMapping("/seller/{sellerId}")
    public List<ProductResponse> bySeller(@PathVariable String sellerId) {
        return productService.getBySeller(sellerId);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable String id) {
        return productService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody ProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(ud.getUsername(), req));
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id,
            @Valid @RequestBody ProductRequest req) {
        return productService.update(id, ud.getUsername(), req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String id) {
        boolean isAdmin = isAdmin(ud);
        productService.delete(id, ud.getUsername(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(UserDetails ud) {
        return userRepo.findById(ud.getUsername())
                .map(u -> u.getRole() == UserRole.ADMIN)
                .orElse(false);
    }
}
