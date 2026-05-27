package com.camplink.dto;

import com.camplink.entity.ShoppingRequestItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShoppingRequestItemDto {
    private String name;
    private int quantity;
    private BigDecimal estimatedPrice;
    private String notes;

    public static ShoppingRequestItemDto from(ShoppingRequestItem i) {
        ShoppingRequestItemDto d = new ShoppingRequestItemDto();
        d.name = i.getName();
        d.quantity = i.getQuantity();
        d.estimatedPrice = i.getEstimatedPrice();
        d.notes = i.getNotes();
        return d;
    }
}
