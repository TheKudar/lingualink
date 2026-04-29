package com.lingualink.chat.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String content,
        LocalDateTime sentAt
) {
}
