package com.camplink.controller;

import com.camplink.dto.NotificationResponse;
import com.camplink.repository.NotificationRepository;
import com.camplink.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepo;
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal UserDetails ud) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(ud.getUsername())
                .stream().map(NotificationResponse::from).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal UserDetails ud) {
        return Map.of("count", notificationRepo.countByUserIdAndReadFalse(ud.getUsername()));
    }

    @PatchMapping("/{id}/read")
    public void markRead(@AuthenticationPrincipal UserDetails ud, @PathVariable String id) {
        notificationService.markRead(id, ud.getUsername());
    }

    @PatchMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal UserDetails ud) {
        notificationService.markAllRead(ud.getUsername());
    }
}
