package com.lingualink.chat.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        Long participantId,
        String participantUsername,
        String participantFirstName,
        String participantLastName,
        String participantAvatarUrl,
        LocalDateTime lastMessageAt
) {
}
