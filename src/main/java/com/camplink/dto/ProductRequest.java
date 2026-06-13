package com.camplink.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name required")
    private String name;

    private String description;

    @NotBlank(message = "Category required")
    private String category;

    @NotNull
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    private boolean available = true;

    /// Legacy single image; still accepted from older clients.
    private String imageUrl;

    /// Ordered image paths. When present this is the source of truth.
    private List<String> imageUrls;
}
