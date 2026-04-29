package com.lingualink.chat.service;

import com.lingualink.chat.dto.ConversationResponse;
import com.lingualink.chat.dto.MessageResponse;
import com.lingualink.chat.entity.Conversation;
import com.lingualink.chat.entity.Message;
import com.lingualink.chat.repository.ConversationRepository;
import com.lingualink.chat.repository.MessageRepository;
import com.lingualink.common.exception.AppException;
import com.lingualink.user.entity.User;
import com.lingualink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ConversationResponse createConversation(Long currentUserId, Long participantId) {
        if (currentUserId.equals(participantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot create a conversation with yourself");
        }

        getUser(currentUserId);
        getUser(participantId);

        Long firstId = Math.min(currentUserId, participantId);
        Long secondId = Math.max(currentUserId, participantId);

        Conversation conversation = conversationRepository.findByParticipantOneIdAndParticipantTwoId(firstId, secondId)
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .participantOneId(firstId)
                        .participantTwoId(secondId)
                        .build()));

        return toConversationResponse(conversation, currentUserId);
    }

    public List<ConversationResponse> getMyConversations(Long currentUserId) {
        return conversationRepository.findByParticipantOneIdOrParticipantTwoIdOrderByCreatedAtDesc(currentUserId, currentUserId)
                .stream()
                .sorted(Comparator.comparing(Conversation::getCreatedAt).reversed())
                .map(conversation -> toConversationResponse(conversation, currentUserId))
                .toList();
    }

    @Transactional
    public MessageResponse sendMessage(Long conversationId, Long senderId, String content) {
        Conversation conversation = getConversationForUser(conversationId, senderId);

        Message message = messageRepository.save(Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .content(content.trim())
                .build());

        return toMessageResponse(message);
    }

    public Page<MessageResponse> getMessages(Long conversationId, Long currentUserId, Pageable pageable) {
        getConversationForUser(conversationId, currentUserId);
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId, pageable)
                .map(this::toMessageResponse);
    }

    private Conversation getConversationForUser(Long conversationId, Long currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException("Conversation not found with id: " + conversationId));

        if (!conversation.getParticipantOneId().equals(currentUserId)
                && !conversation.getParticipantTwoId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this conversation");
        }

        return conversation;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found with id: " + userId));
    }

    private ConversationResponse toConversationResponse(Conversation conversation, Long currentUserId) {
        Long otherUserId = conversation.getParticipantOneId().equals(currentUserId)
                ? conversation.getParticipantTwoId()
                : conversation.getParticipantOneId();
        User otherUser = getUser(otherUserId);

        return new ConversationResponse(
                conversation.getId(),
                conversation.getParticipantOneId(),
                conversation.getParticipantTwoId(),
                otherUserId,
                otherUser.getUsername(),
                conversation.getCreatedAt()
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSenderId(),
                message.getContent(),
                message.getSentAt()
        );
    }
}
