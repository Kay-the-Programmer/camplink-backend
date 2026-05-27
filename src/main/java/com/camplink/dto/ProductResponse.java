package com.camplink.dto;

import com.camplink.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        r.imageUrl    = p.getImageUrl();
        r.createdAt   = p.getCreatedAt();
        return r;
    }
}
