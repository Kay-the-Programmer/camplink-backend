package com.camplink.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank private String sellerId;
    @NotBlank private String orderId;
    @Min(1) @Max(5) private int rating;
    private String comment;
}
