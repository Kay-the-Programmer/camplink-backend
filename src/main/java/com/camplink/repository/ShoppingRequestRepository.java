package com.camplink.repository;

import com.camplink.entity.RequestStatus;
import com.camplink.entity.ShoppingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingRequestRepository extends JpaRepository<ShoppingRequest, String> {
    List<ShoppingRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    List<ShoppingRequest> findByRequesterIdOrderByCreatedAtDesc(String requesterId);
    List<ShoppingRequest> findByRunnerIdOrderByCreatedAtDesc(String runnerId);
}
