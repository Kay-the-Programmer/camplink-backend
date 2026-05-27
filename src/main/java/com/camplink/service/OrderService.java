package com.camplink.service;

import com.camplink.dto.*;
import com.camplink.entity.*;
import com.camplink.exception.AppException;
import com.camplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    @Transactional
    public OrderResponse place(String buyerId, OrderRequest req) {
        User buyer = userRepo.findById(buyerId)
                .orElseThrow(() -> AppException.notFound("Buyer not found"));

        // Resolve items and determine seller
        String sellerId = null;
        List<OrderLine> lines = new java.util.ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderLineRequest lr : req.getItems()) {
            Product p = productRepo.findById(lr.getProductId())
                    .orElseThrow(() -> AppException.notFound("Product not found: " + lr.getProductId()));
            if (!p.isAvailable()) throw AppException.badRequest("Product unavailable: " + p.getName());
            if (sellerId == null) sellerId = p.getSeller().getId();
            else if (!sellerId.equals(p.getSeller().getId())) {
                throw AppException.badRequest("All items must be from the same seller");
            }
            OrderLine line = OrderLine.builder()
                    .productId(p.getId())
                    .productName(p.getName())
                    .price(p.getPrice())
                    .quantity(lr.getQuantity())
                    .build();
            lines.add(line);
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(lr.getQuantity())));
        }

        User seller = userRepo.findById(sellerId)
                .orElseThrow(() -> AppException.notFound("Seller not found"));

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyer(buyer)
                .seller(seller)
                .items(lines)
                .total(total)
                .status(OrderStatus.PENDING)
                .deliveryMethod(req.getDeliveryMethod())
                .deliveryLocation(req.getDeliveryLocation())
                .paymentMethod(req.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
        orderRepo.save(order);

        notificationService.push(sellerId, NotificationType.ORDER_PLACED,
                "New order from " + buyer.getFullName(),
                "You have a new order. Tap to view.", order.getId());

        return OrderResponse.from(order);
    }

    public List<OrderResponse> forBuyer(String buyerId) {
        return orderRepo.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream().map(OrderResponse::from).toList();
    }

    public List<OrderResponse> forSeller(String sellerId) {
        return orderRepo.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream().map(OrderResponse::from).toList();
    }

    public List<OrderResponse> all() {
        return orderRepo.findAllByOrderByCreatedAtDesc()
                .stream().map(OrderResponse::from).toList();
    }

    @Transactional
    public OrderResponse updateStatus(String orderId, String callerId, StatusUpdateRequest req) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Order not found"));
        if (!order.getSeller().getId().equals(callerId)) {
            throw AppException.forbidden("Only the seller can update order status");
        }
        order.setStatus(req.getStatus());
        orderRepo.save(order);

        // Notify buyer
        String buyerId = order.getBuyer().getId();
        switch (req.getStatus()) {
            case CONFIRMED  -> notificationService.push(buyerId, NotificationType.ORDER_CONFIRMED,
                    "Order confirmed", "Your order has been confirmed.", orderId);
            case DELIVERED  -> notificationService.push(buyerId, NotificationType.ORDER_DELIVERED,
                    "Order delivered", "Your order has been delivered.", orderId);
            case CANCELLED  -> notificationService.push(buyerId, NotificationType.ORDER_CANCELLED,
                    "Order cancelled", "Your order was cancelled.", orderId);
            default -> {}
        }

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse markPaid(String orderId, String callerId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Order not found"));
        if (!order.getSeller().getId().equals(callerId)) {
            throw AppException.forbidden("Only the seller can mark payment");
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepo.save(order);

        notificationService.push(order.getBuyer().getId(), NotificationType.PAYMENT_CONFIRMED,
                "Payment confirmed", "Payment for your order has been confirmed.", orderId);

        return OrderResponse.from(order);
    }
}
