package com.camplink.controller;

import com.camplink.dto.*;
import com.camplink.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chat", description = "Direct messaging between users")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @Operation(
        summary = "Start or reopen a conversation",
        description = "Creates a new 1-on-1 conversation with `otherUserId`, or returns the existing one " +
                      "if a conversation between the two users already exists. Idempotent."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Conversation created or existing conversation returned"),
        @ApiResponse(responseCode = "400", description = "Missing otherUserId"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT"),
        @ApiResponse(responseCode = "404", description = "Other user not found")
    })
    public ResponseEntity<ConversationResponse> start(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody StartConversationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.ensureConversation(ud.getUsername(), req.getOtherUserId()));
    }

    @GetMapping
    @Operation(
        summary = "List my conversations",
        description = "Returns all conversations the authenticated user participates in, sorted by most recent message."
    )
    @ApiResponse(responseCode = "200", description = "Conversation list returned")
    public List<ConversationResponse> myConversations(@AuthenticationPrincipal UserDetails ud) {
        return chatService.forUser(ud.getUsername());
    }

    @GetMapping("/{convoId}/messages")
    @Operation(
        summary = "Get messages in a conversation",
        description = "Returns all messages in the conversation in chronological order. " +
                      "The authenticated user must be a participant."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message list returned"),
        @ApiResponse(responseCode = "403", description = "Not a participant in this conversation"),
        @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    public List<MessageResponse> messages(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Conversation ID") @PathVariable String convoId) {
        return chatService.messages(convoId, ud.getUsername());
    }

    @PostMapping("/{convoId}/messages")
    @Operation(
        summary = "Send a message",
        description = "Appends a new text message to the conversation. " +
                      "The authenticated user must be a participant."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Message sent"),
        @ApiResponse(responseCode = "400", description = "Empty message text"),
        @ApiResponse(responseCode = "403", description = "Not a participant in this conversation"),
        @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    public ResponseEntity<MessageResponse> send(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Conversation ID") @PathVariable String convoId,
            @Valid @RequestBody MessageRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.send(convoId, ud.getUsername(), req));
    }
}
