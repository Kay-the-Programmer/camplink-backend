package com.camplink.dto;

import com.camplink.entity.Conversation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationResponse {
    private String id;
    private List<ParticipantInfo> participants;
    private String lastMessage;
    private String lastSenderId;
    private LocalDateTime updatedAt;

    @Data
    public static class ParticipantInfo {
        private String id;
        private String name;
        private String photoUrl;
    }

    public static ConversationResponse from(Conversation c) {
        ConversationResponse r = new ConversationResponse();
        r.id          = c.getId();
        r.lastMessage = c.getLastMessage();
        r.lastSenderId= c.getLastSender() != null ? c.getLastSender().getId() : null;
        r.updatedAt   = c.getUpdatedAt();
        r.participants= c.getParticipants().stream().map(u -> {
            ParticipantInfo p = new ParticipantInfo();
            p.id       = u.getId();
            p.name     = u.getFullName();
            p.photoUrl = u.getPhotoUrl();
            return p;
        }).toList();
        return r;
    }
}
