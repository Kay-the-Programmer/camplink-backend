package com.camplink.dto;

import com.camplink.entity.Notification;
import com.camplink.entity.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private String id;
    private NotificationType type;
    private String title;
    private String body;
    private String orderId;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.id        = n.getId();
        r.type      = n.getType();
        r.title     = n.getTitle();
        r.body      = n.getBody();
        r.orderId   = n.getOrderId();
        r.read      = n.isRead();
        r.createdAt = n.getCreatedAt();
        return r;
    }
}
