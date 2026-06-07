package com.camplink.service;

import com.camplink.entity.DeviceToken;
import com.camplink.repository.DeviceTokenRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Sends Firebase Cloud Messaging push notifications and manages device tokens.
 *
 * <p>Firebase is initialised from a service-account JSON whose path is given by
 * {@code firebase.credentials-path} (or the standard
 * {@code GOOGLE_APPLICATION_CREDENTIALS} env var). If no credentials are
 * configured, push is silently disabled — in-app notifications still work, so
 * the app is fully functional without Firebase set up.</p>
 */
@Service
@Slf4j
public class PushService {

    private final DeviceTokenRepository tokenRepo;
    private final FirebaseMessaging messaging; // null when not configured

    public PushService(DeviceTokenRepository tokenRepo,
                       @Value("${firebase.credentials-path:}") String credentialsPath) {
        this.tokenRepo = tokenRepo;
        this.messaging = initMessaging(credentialsPath);
    }

    private FirebaseMessaging initMessaging(String credentialsPath) {
        try {
            GoogleCredentials credentials;
            if (credentialsPath != null && !credentialsPath.isBlank()) {
                try (InputStream in = new FileInputStream(credentialsPath)) {
                    credentials = GoogleCredentials.fromStream(in);
                }
            } else {
                // Falls back to GOOGLE_APPLICATION_CREDENTIALS if present.
                credentials = GoogleCredentials.getApplicationDefault();
            }
            FirebaseApp app = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(FirebaseOptions.builder()
                            .setCredentials(credentials).build())
                    : FirebaseApp.getInstance();
            log.info("Firebase Cloud Messaging initialised — push notifications enabled.");
            return FirebaseMessaging.getInstance(app);
        } catch (Exception e) {
            log.warn("Firebase not configured ({}). Push notifications are disabled; "
                    + "in-app notifications still work.", e.getMessage());
            return null;
        }
    }

    public boolean isEnabled() {
        return messaging != null;
    }

    // ── Token management ──────────────────────────────────────────────────────

    @Transactional
    public void registerToken(String userId, String token) {
        if (token == null || token.isBlank()) return;
        tokenRepo.findByToken(token).ifPresentOrElse(
                existing -> {
                    existing.setUserId(userId);
                    tokenRepo.save(existing);
                },
                () -> tokenRepo.save(DeviceToken.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .token(token)
                        .build()));
    }

    @Transactional
    public void removeToken(String token) {
        if (token != null && !token.isBlank()) tokenRepo.deleteByToken(token);
    }

    // ── Sending ───────────────────────────────────────────────────────────────

    /** Best-effort push to every device the user has registered. Never throws. */
    @Transactional
    public void sendToUser(String userId, String title, String body, String type, String refId) {
        if (messaging == null) return;
        List<DeviceToken> tokens = tokenRepo.findByUserId(userId);
        for (DeviceToken dt : tokens) {
            try {
                messaging.send(Message.builder()
                        .setToken(dt.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putData("type", type == null ? "" : type)
                        .putData("refId", refId == null ? "" : refId)
                        .build());
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode code = e.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED
                        || code == MessagingErrorCode.INVALID_ARGUMENT) {
                    // Stale token — drop it so we stop trying.
                    tokenRepo.deleteByToken(dt.getToken());
                } else {
                    log.warn("FCM send failed: {}", e.getMessage());
                }
            }
        }
    }
}
