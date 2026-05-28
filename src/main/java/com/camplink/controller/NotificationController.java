package com.camplink.controller;

import com.camplink.dto.NotificationResponse;
import com.camplink.repository.NotificationRepository;
import com.camplink.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notification management")
public class NotificationController {

    private final NotificationRepository notificationRepo;
    private final NotificationService notificationService;

    @GetMapping
    @Operation(
        summary = "List my notifications",
        description = "Returns all notifications for the authenticated user, newest first. " +
                      "Notification types: `ORDER_PLACED`, `ORDER_CONFIRMED`, `ORDER_DELIVERED`, " +
                      "`ORDER_CANCELLED`, `PAYMENT_CONFIRMED`, `MESSAGE`, `REQUEST_ACCEPTED`, " +
                      "`REQUEST_FULFILLED`, `OTHER`."
    )
    @ApiResponse(responseCode = "200", description = "Notification list returned")
    public List<NotificationResponse> list(@AuthenticationPrincipal UserDetails ud) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(ud.getUsername())
                .stream().map(NotificationResponse::from).toList();
    }

    @GetMapping("/unread-count")
    @Operation(
        summary = "Get unread notification count",
        description = "Returns `{ \"count\": N }` where N is the number of unread notifications. " +
                      "Useful for badge rendering on the notification bell."
    )
    @ApiResponse(responseCode = "200", description = "Count returned")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal UserDetails ud) {
        return Map.of("count", notificationRepo.countByUserIdAndReadFalse(ud.getUsername()));
    }

    @PatchMapping("/{id}/read")
    @Operation(
        summary = "Mark a notification as read",
        description = "Sets the `read` flag to true on the specified notification. " +
                      "The notification must belong to the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    public void markRead(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Notification ID") @PathVariable String id) {
        notificationService.markRead(id, ud.getUsername());
    }

    @PatchMapping("/read-all")
    @Operation(
        summary = "Mark all notifications as read",
        description = "Sets the `read` flag to true on every unread notification for the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "All notifications marked as read")
    public void markAllRead(@AuthenticationPrincipal UserDetails ud) {
        notificationService.markAllRead(ud.getUsername());
    }
}
