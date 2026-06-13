package com.camplink.service;

import com.camplink.dto.*;
import com.camplink.entity.Product;
import com.camplink.entity.User;
import com.camplink.exception.AppException;
import com.camplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public List<ProductResponse> getAll() {
        return productRepo.findAllByOrderByCreatedAtDesc()
                .stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getById(String id) {
        return ProductResponse.from(
                productRepo.findById(id).orElseThrow(() -> AppException.notFound("Product not found")));
    }

    public List<ProductResponse> getBySeller(String sellerId) {
        return productRepo.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream().map(ProductResponse::from).toList();
    }

    @Transactional
    public ProductResponse create(String sellerId, ProductRequest req) {
        User seller = userRepo.findById(sellerId)
                .orElseThrow(() -> AppException.notFound("Seller not found"));
        List<String> images = resolveImages(req);
        Product p = Product.builder()
                .id(UUID.randomUUID().toString())
                .seller(seller)
                .name(req.getName())
                .description(req.getDescription())
                .category(req.getCategory())
                .price(req.getPrice())
                .available(req.isAvailable())
                .imageUrls(images)
                .imageUrl(images.isEmpty() ? null : images.get(0))
                .build();
        return ProductResponse.from(productRepo.save(p));
    }

    @Transactional
    public ProductResponse update(String productId, String callerId, ProductRequest req) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> AppException.notFound("Product not found"));
        if (!p.getSeller().getId().equals(callerId)) throw AppException.forbidden("Not your product");
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setCategory(req.getCategory());
        p.setPrice(req.getPrice());
        p.setAvailable(req.isAvailable());
        // Only touch images when the client actually sent some, so a partial
        // update never wipes the gallery.
        if ((req.getImageUrls() != null && !req.getImageUrls().isEmpty())
                || req.getImageUrl() != null) {
            List<String> images = resolveImages(req);
            p.getImageUrls().clear();
            p.getImageUrls().addAll(images);
            p.setImageUrl(images.isEmpty() ? null : images.get(0));
        }
        return ProductResponse.from(productRepo.save(p));
    }

    /// Prefer the new {@code imageUrls} list; fall back to the single legacy
    /// {@code imageUrl} so older clients keep working.
    private List<String> resolveImages(ProductRequest req) {
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            return new ArrayList<>(req.getImageUrls());
        }
        List<String> single = new ArrayList<>();
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {
            single.add(req.getImageUrl());
        }
        return single;
    }

    @Transactional
    public void delete(String productId, String callerId, boolean isAdmin) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> AppException.notFound("Product not found"));
        if (!isAdmin && !p.getSeller().getId().equals(callerId)) {
            throw AppException.forbidden("Not your product");
        }
        productRepo.delete(p);
    }
}
