package com.camplink.repository;

import com.camplink.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findAllByOrderByCreatedAtDesc();
    List<Product> findBySellerIdOrderByCreatedAtDesc(String sellerId);
}
