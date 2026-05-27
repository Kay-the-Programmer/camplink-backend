package com.camplink.service;

import com.camplink.dto.*;
import com.camplink.entity.*;
import com.camplink.exception.AppException;
import com.camplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository convoRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    /** Deterministic conversation ID: sorted UIDs joined by '_' */
    private String convoId(String a, String b) {
        List<String> ids = new ArrayList<>(List.of(a, b));
        Collections.sort(ids);
        return String.join("_", ids);
    }

    @Transactional
    public ConversationResponse ensureConversation(String callerId, String otherUserId) {
        String id = convoId(callerId, otherUserId);
        return convoRepo.findById(id)
                .map(ConversationResponse::from)
                .orElseGet(() -> {
                    User me = userRepo.findById(callerId)
                            .orElseThrow(() -> AppException.notFound("User not found"));
                    User other = userRepo.findById(otherUserId)
                            .orElseThrow(() -> AppException.notFound("Other user not found"));
                    Conversation c = Conversation.builder()
                            .id(id)
                            .participants(List.of(me, other))
                            .build();
                    return ConversationResponse.from(convoRepo.save(c));
                });
    }

    public List<ConversationResponse> forUser(String userId) {
        return convoRepo.findByParticipantId(userId)
                .stream().map(ConversationResponse::from).toList();
    }

    public List<MessageResponse> messages(String convoId, String callerId) {
        Conversation c = convoRepo.findById(convoId)
                .orElseThrow(() -> AppException.notFound("Conversation not found"));
        boolean participant = c.getParticipants().stream()
                .anyMatch(u -> u.getId().equals(callerId));
        if (!participant) throw AppException.forbidden("Not a participant");
        return messageRepo.findByConversationIdOrderBySentAtAsc(convoId)
                .stream().map(MessageResponse::from).toList();
    }

    @Transactional
    public MessageResponse send(String convoId, String senderId, MessageRequest req) {
        Conversation c = convoRepo.findById(convoId)
                .orElseThrow(() -> AppException.notFound("Conversation not found"));
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> AppException.notFound("Sender not found"));

        boolean participant = c.getParticipants().stream()
                .anyMatch(u -> u.getId().equals(senderId));
        if (!participant) throw AppException.forbidden("Not a participant");

        Message msg = Message.builder()
                .id(UUID.randomUUID().toString())
                .conversation(c)
                .sender(sender)
                .text(req.getText())
                .build();
        messageRepo.save(msg);

        c.setLastMessage(req.getText());
        c.setLastSender(sender);
        c.setUpdatedAt(LocalDateTime.now());
        convoRepo.save(c);

        // Notify the other participant
        c.getParticipants().stream()
                .filter(u -> !u.getId().equals(senderId))
                .findFirst()
                .ifPresent(recipient -> notificationService.push(
                        recipient.getId(), NotificationType.MESSAGE,
                        "New message from " + sender.getFullName(),
                        req.getText(), null));

        return MessageResponse.from(msg);
    }
}
