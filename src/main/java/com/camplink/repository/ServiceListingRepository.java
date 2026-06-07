package com.camplink.repository;

import com.camplink.entity.ServiceListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceListingRepository extends JpaRepository<ServiceListing, String> {

    List<ServiceListing> findAllByOrderByCreatedAtDesc();

    List<ServiceListing> findByProviderIdOrderByCreatedAtDesc(String providerId);
}
