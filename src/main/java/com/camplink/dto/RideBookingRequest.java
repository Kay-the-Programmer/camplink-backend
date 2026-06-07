package com.camplink.dto;

import com.camplink.entity.DropOff;
import com.camplink.entity.RideType;
import com.camplink.entity.RouteDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RideBookingRequest {
    @NotBlank(message = "from required")
    private String from;

    @NotBlank(message = "to required")
    private String to;

    @NotNull(message = "direction required")
    private RouteDirection direction;

    @NotNull(message = "dropOff required")
    private DropOff dropOff;

    @NotNull(message = "type required")
    private RideType type;

    private LocalDateTime scheduledAt;
    private Integer seats;
    private String note;
}
