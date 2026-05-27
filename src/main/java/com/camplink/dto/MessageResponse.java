package com.camplink.dto;

import com.camplink.entity.Message;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String text;
    private LocalDateTime sentAt;

    public static MessageResponse from(Message m) {
        MessageResponse r = new MessageResponse();
        r.id             = m.getId();
        r.conversationId = m.getConversation().getId();
        r.senderId       = m.getSender().getId();
        r.senderName     = m.getSender().getFullName();
        r.text           = m.getText();
        r.sentAt         = m.getSentAt();
        return r;
    }
}
