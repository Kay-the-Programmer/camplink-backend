package com.camplink.service;

import com.camplink.entity.*;
import com.camplink.exception.AppException;
import com.camplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;

    @Transactional
    public void push(String userId, NotificationType type, String title, String body, String orderId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        Notification n = Notification.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .orderId(orderId)
                .read(false)
                .build();
        notificationRepo.save(n);
    }

    @Transactional
    public void markRead(String notificationId, String userId) {
        Notification n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> AppException.notFound("Notification not found"));
        if (!n.getUser().getId().equals(userId)) throw AppException.forbidden("Not yours");
        n.setRead(true);
        notificationRepo.save(n);
    }

    @Transactional
    public void markAllRead(String userId) {
        notificationRepo.markAllReadByUserId(userId);
    }
}
