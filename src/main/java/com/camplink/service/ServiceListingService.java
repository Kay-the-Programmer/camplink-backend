package com.camplink.service;

import com.camplink.dto.ServiceListingRequest;
import com.camplink.dto.ServiceListingResponse;
import com.camplink.entity.ServiceListing;
import com.camplink.entity.User;
import com.camplink.entity.UserRole;
import com.camplink.exception.AppException;
import com.camplink.repository.ServiceListingRepository;
import com.camplink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceListingService {

    private final ServiceListingRepository repo;
    private final UserRepository userRepo;

    public List<ServiceListingResponse> all() {
        return repo.findAllByOrderByCreatedAtDesc()
                .stream().map(ServiceListingResponse::from).toList();
    }

    public List<ServiceListingResponse> mine(String userId) {
        return repo.findByProviderIdOrderByCreatedAtDesc(userId)
                .stream().map(ServiceListingResponse::from).toList();
    }

    @Transactional
    public ServiceListingResponse create(String userId, ServiceListingRequest req) {
        User provider = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        ServiceListing s = ServiceListing.builder()
                .id(UUID.randomUUID().toString())
                .provider(provider)
                .title(req.getTitle().trim())
                .description(req.getDescription().trim())
                .category(req.getCategory())
                .price(req.getPrice() != null ? BigDecimal.valueOf(req.getPrice()) : null)
                .priceNote(req.getPriceNote())
                .available(req.getAvailable() == null || req.getAvailable())
                .build();

        // saveAndFlush so @CreationTimestamp is populated before we serialise.
        return ServiceListingResponse.from(repo.saveAndFlush(s));
    }

    @Transactional
    public ServiceListingResponse update(String id, String callerId, Map<String, Object> body) {
        ServiceListing s = requireOwner(id, callerId);
        Object available = body.get("available");
        if (available instanceof Boolean b) {
            s.setAvailable(b);
        }
        return ServiceListingResponse.from(repo.save(s));
    }

    @Transactional
    public void delete(String id, String callerId) {
        requireOwner(id, callerId);
        repo.deleteById(id);
    }

    private ServiceListing requireOwner(String id, String callerId) {
        ServiceListing s = repo.findById(id)
                .orElseThrow(() -> AppException.notFound("Service not found"));
        boolean isAdmin = userRepo.findById(callerId)
                .map(u -> u.getRole() == UserRole.ADMIN)
                .orElse(false);
        if (!s.getProvider().getId().equals(callerId) && !isAdmin) {
            throw AppException.forbidden("You can only modify your own service listings");
        }
        return s;
    }
}
