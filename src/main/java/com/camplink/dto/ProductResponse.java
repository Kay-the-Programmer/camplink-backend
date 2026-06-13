package com.camplink.dto;

import com.camplink.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductResponse {
    private String id;
    private String sellerId;
    private String sellerName;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private boolean available;
    private String imageUrl;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product p) {
        ProductResponse r = new ProductResponse();
        r.id          = p.getId();
        r.sellerId    = p.getSeller().getId();
        r.sellerName  = p.getSeller().getFullName();
        r.name        = p.getName();
        r.description = p.getDescription();
        r.category    = p.getCategory();
        r.price       = p.getPrice();
        r.available   = p.isAvailable();
        r.imageUrls   = p.getImageUrls() == null
                ? new ArrayList<>() : new ArrayList<>(p.getImageUrls());
        // Mirror the first image into the legacy single field.
        r.imageUrl    = r.imageUrls.isEmpty() ? p.getImageUrl() : r.imageUrls.get(0);
        r.createdAt   = p.getCreatedAt();
        return r;
    }
}
