package com.camplink.service;

import com.camplink.dto.*;
import com.camplink.entity.Review;
import com.camplink.exception.AppException;
import com.camplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;

    @Transactional
    public ReviewResponse create(String buyerId, ReviewRequest req) {
        if (reviewRepo.existsByOrderId(req.getOrderId())) {
            throw AppException.conflict("Order already reviewed");
        }
        var buyer  = userRepo.findById(buyerId).orElseThrow(() -> AppException.notFound("Buyer not found"));
        var seller = userRepo.findById(req.getSellerId()).orElseThrow(() -> AppException.notFound("Seller not found"));
        Review r = Review.builder()
                .id(UUID.randomUUID().toString())
                .seller(seller)
                .buyer(buyer)
                .orderId(req.getOrderId())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();
        return ReviewResponse.from(reviewRepo.save(r));
    }

    public List<ReviewResponse> forSeller(String sellerId) {
        return reviewRepo.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream().map(ReviewResponse::from).toList();
    }

    public Map<String, Object> ratingForSeller(String sellerId) {
        Double avg = reviewRepo.averageRatingBySellerId(sellerId);
        long count = reviewRepo.countBySellerId(sellerId);
        Map<String, Object> result = new HashMap<>();
        result.put("average", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        result.put("count", count);
        return result;
    }

    public boolean orderReviewed(String orderId) {
        return reviewRepo.existsByOrderId(orderId);
    }
}
