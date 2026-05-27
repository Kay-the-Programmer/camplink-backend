package com.camplink.dto;

import com.camplink.entity.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private String id;
    private String buyerId;
    private String buyerName;
    private String buyerPhone;
    private String sellerId;
    private String sellerName;
    private List<LineResponse> items;
    private BigDecimal total;
    private OrderStatus status;
    private DeliveryMethod deliveryMethod;
    private String deliveryLocation;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;

    @Data
    public static class LineResponse {
        private String productId;
        private String productName;
        private BigDecimal price;
        private int quantity;

        public static LineResponse from(OrderLine l) {
            LineResponse r = new LineResponse();
            r.productId   = l.getProductId();
            r.productName = l.getProductName();
            r.price       = l.getPrice();
            r.quantity    = l.getQuantity();
            return r;
        }
    }

    public static OrderResponse from(Order o) {
        OrderResponse r = new OrderResponse();
        r.id              = o.getId();
        r.buyerId         = o.getBuyer().getId();
        r.buyerName       = o.getBuyer().getFullName();
        r.buyerPhone      = o.getBuyer().getPhone();
        r.sellerId        = o.getSeller().getId();
        r.sellerName      = o.getSeller().getFullName();
        r.items           = o.getItems().stream().map(LineResponse::from).toList();
        r.total           = o.getTotal();
        r.status          = o.getStatus();
        r.deliveryMethod  = o.getDeliveryMethod();
        r.deliveryLocation= o.getDeliveryLocation();
        r.paymentMethod   = o.getPaymentMethod();
        r.paymentStatus   = o.getPaymentStatus();
        r.createdAt       = o.getCreatedAt();
        return r;
    }
}
