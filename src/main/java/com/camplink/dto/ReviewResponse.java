package com.camplink.dto;

import com.camplink.entity.Review;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private String id;
    private String sellerId;
    private String buyerId;
    private String buyerName;
    private String orderId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review r) {
        ReviewResponse res = new ReviewResponse();
        res.id        = r.getId();
        res.sellerId  = r.getSeller().getId();
        res.buyerId   = r.getBuyer().getId();
        res.buyerName = r.getBuyer().getFullName();
        res.orderId   = r.getOrderId();
        res.rating    = r.getRating();
        res.comment   = r.getComment();
        res.createdAt = r.getCreatedAt();
        return res;
    }
}
