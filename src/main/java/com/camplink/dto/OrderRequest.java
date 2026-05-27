package com.camplink.dto;

import com.camplink.entity.DeliveryMethod;
import com.camplink.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotEmpty(message = "Order must have at least one item")
    private List<OrderLineRequest> items;

    @NotNull private DeliveryMethod deliveryMethod;
    private String deliveryLocation;

    @NotNull private PaymentMethod paymentMethod;
}
