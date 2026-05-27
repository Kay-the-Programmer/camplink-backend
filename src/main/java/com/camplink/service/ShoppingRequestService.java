package com.camplink.service;

import com.camplink.dto.CreateShoppingRequestRequest;
import com.camplink.dto.ShoppingRequestItemDto;
import com.camplink.dto.ShoppingRequestResponse;
import com.camplink.entity.*;
import com.camplink.exception.AppException;
import com.camplink.repository.ShoppingRequestRepository;
import com.camplink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingRequestService {

    private final ShoppingRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    @Transactional
    public ShoppingRequestResponse create(String userId, CreateShoppingRequestRequest req) {
        User requester = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        List<ShoppingRequestItem> items = req.getItems().stream()
                .map(d -> ShoppingRequestItem.builder()
                        .name(d.getName())
                        .quantity(d.getQuantity())
                        .estimatedPrice(d.getEstimatedPrice())
                        .notes(d.getNotes())
                        .build())
                .collect(Collectors.toList());

        ShoppingRequest sr = ShoppingRequest.builder()
                .id(UUID.randomUUID().toString())
                .requester(requester)
                .title(req.getTitle())
                .items(items)
                .deliveryHostel(req.getDeliveryHostel())
                .deliveryRoom(req.getDeliveryRoom())
                .budget(req.getBudget())
                .note(req.getNote())
                .runnerFee(req.getRunnerFee())
                .status(RequestStatus.OPEN)
                .build();

        return ShoppingRequestResponse.from(requestRepo.save(sr));
    }

    public List<ShoppingRequestResponse> getOpen() {
        return requestRepo.findByStatusOrderByCreatedAtDesc(RequestStatus.OPEN)
                .stream().map(ShoppingRequestResponse::from).collect(Collectors.toList());
    }

    public List<ShoppingRequestResponse> getMine(String userId) {
        return requestRepo.findByRequesterIdOrderByCreatedAtDesc(userId)
                .stream().map(ShoppingRequestResponse::from).collect(Collectors.toList());
    }

    public List<ShoppingRequestResponse> getRunning(String userId) {
        return requestRepo.findByRunnerIdOrderByCreatedAtDesc(userId)
                .stream().map(ShoppingRequestResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ShoppingRequestResponse accept(String requestId, String runnerId) {
        ShoppingRequest sr = findOrThrow(requestId);
        if (!sr.getStatus().equals(RequestStatus.OPEN))
            throw AppException.badRequest("Request is no longer open");
        if (sr.getRequester().getId().equals(runnerId))
            throw AppException.badRequest("You cannot run your own request");

        User runner = userRepo.findById(runnerId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        sr.setRunner(runner);
        sr.setStatus(RequestStatus.ACCEPTED);
        ShoppingRequest saved = requestRepo.save(sr);

        notificationService.push(
                sr.getRequester().getId(),
                NotificationType.REQUEST_ACCEPTED,
                "Request accepted",
                runner.getFullName() + " will run your request: " + sr.getTitle(),
                null
        );

        return ShoppingRequestResponse.from(saved);
    }

    @Transactional
    public ShoppingRequestResponse fulfill(String requestId, String runnerId) {
        ShoppingRequest sr = findOrThrow(requestId);
        if (!sr.getStatus().equals(RequestStatus.ACCEPTED))
            throw AppException.badRequest("Request is not in ACCEPTED state");
        if (!sr.getRunner().getId().equals(runnerId))
            throw AppException.forbidden("Only the assigned runner can mark this fulfilled");

        sr.setStatus(RequestStatus.FULFILLED);
        ShoppingRequest saved = requestRepo.save(sr);

        notificationService.push(
                sr.getRequester().getId(),
                NotificationType.REQUEST_FULFILLED,
                "Items delivered!",
                sr.getRunner().getFullName() + " has delivered your request: " + sr.getTitle(),
                null
        );

        return ShoppingRequestResponse.from(saved);
    }

    @Transactional
    public void cancel(String requestId, String userId) {
        ShoppingRequest sr = findOrThrow(requestId);
        if (!sr.getRequester().getId().equals(userId))
            throw AppException.forbidden("Not your request");
        if (sr.getStatus().equals(RequestStatus.FULFILLED))
            throw AppException.badRequest("Fulfilled requests cannot be cancelled");
        sr.setStatus(RequestStatus.CANCELLED);
        requestRepo.save(sr);
    }

    private ShoppingRequest findOrThrow(String id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Request not found"));
    }
}
