package com.camplink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartConversationRequest {
    @NotBlank(message = "otherUserId required")
    private String otherUserId;
}
