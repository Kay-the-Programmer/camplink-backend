package com.camplink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank private String text;
}
