package com.camplink.repository;

import com.camplink.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findBySellerIdOrderByCreatedAtDesc(String sellerId);

    boolean existsByOrderId(String orderId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.seller.id = :sellerId")
    Double averageRatingBySellerId(@Param("sellerId") String sellerId);

    long countBySellerId(String sellerId);
}
