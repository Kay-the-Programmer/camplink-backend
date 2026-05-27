package com.camplink.dto;

import com.camplink.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull private OrderStatus status;
}
