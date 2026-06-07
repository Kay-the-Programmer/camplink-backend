package com.camplink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServiceListingRequest {
    @NotBlank(message = "title required")
    private String title;

    @NotBlank(message = "description required")
    private String description;

    @NotBlank(message = "category required")
    private String category;

    private Double price;
    private String priceNote;
    private Boolean available;
}
