package com.lingualink.chat.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        Long participantOneId,
        Long participantTwoId,
        Long otherUserId,
        String otherUsername,
        LocalDateTime createdAt
) {
}
