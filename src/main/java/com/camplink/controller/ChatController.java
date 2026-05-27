package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ConversationResponse> start(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody StartConversationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.ensureConversation(ud.getUsername(), req.getOtherUserId()));
    }

    @GetMapping
    public List<ConversationResponse> myConversations(@AuthenticationPrincipal UserDetails ud) {
        return chatService.forUser(ud.getUsername());
    }

    @GetMapping("/{convoId}/messages")
    public List<MessageResponse> messages(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String convoId) {
        return chatService.messages(convoId, ud.getUsername());
    }

    @PostMapping("/{convoId}/messages")
    public ResponseEntity<MessageResponse> send(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable String convoId,
            @Valid @RequestBody MessageRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.send(convoId, ud.getUsername(), req));
    }
}
