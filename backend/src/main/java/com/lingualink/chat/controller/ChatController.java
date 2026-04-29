package com.lingualink.chat.controller;

import com.lingualink.chat.dto.ConversationCreateRequest;
import com.lingualink.chat.dto.ConversationResponse;
import com.lingualink.chat.dto.MessageCreateRequest;
import com.lingualink.chat.dto.MessageResponse;
import com.lingualink.chat.service.ChatService;
import com.lingualink.common.exception.AppException;
import com.lingualink.user.entity.User;
import com.lingualink.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody ConversationCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.createConversation(getCurrentUserId(currentUser), request.participantId()));
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getMyConversations(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(chatService.getMyConversations(getCurrentUserId(currentUser)));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(conversationId, getCurrentUserId(currentUser), request.content()));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserDetails currentUser,
            @PageableDefault(size = 50, sort = "sentAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, getCurrentUserId(currentUser), pageable));
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new AppException("User not found"));
        return user.getId();
    }
}
