package com.camplink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateShoppingRequestRequest {
    @NotBlank
    private String title;

    @NotEmpty
    private List<ShoppingRequestItemDto> items;

    @NotBlank
    private String deliveryHostel;

    private String deliveryRoom;
    private BigDecimal budget;
    private String note;
    private BigDecimal runnerFee;
}
