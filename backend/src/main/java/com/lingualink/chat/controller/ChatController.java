package com.lingualink.chat.controller;

import com.lingualink.chat.dto.ConversationCreateRequest;
import com.lingualink.chat.dto.ConversationResponse;
import com.lingualink.chat.dto.MessageCreateRequest;
import com.lingualink.chat.dto.MessageResponse;
import com.lingualink.chat.service.ChatService;
import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.common.exception.AppException;
import com.lingualink.user.entity.User;
import com.lingualink.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Chat")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a conversation", description = "Creates or returns a conversation with another user.")
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody ConversationCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.createConversation(getCurrentUserId(currentUser), request.participantId()));
    }

    @GetMapping
    @Operation(summary = "List my conversations", description = "Returns conversations for the authenticated user.")
    public ResponseEntity<List<ConversationResponse>> getMyConversations(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(chatService.getMyConversations(getCurrentUserId(currentUser)));
    }

    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Send a message", description = "Sends a message in a conversation the authenticated user belongs to.")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(conversationId, getCurrentUserId(currentUser), request.content()));
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "List conversation messages", description = "Returns paged messages for a conversation the authenticated user belongs to.")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @ParameterObject @PageableDefault(size = 50, sort = "sentAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, getCurrentUserId(currentUser), pageable));
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new AppException("User not found"));
        return user.getId();
    }
}
