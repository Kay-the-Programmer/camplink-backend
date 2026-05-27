package com.camplink.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderLineRequest {
    @NotBlank private String productId;
    @Min(1)   private int quantity;
}
