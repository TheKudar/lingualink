package com.lingualink.chat.dto;

import jakarta.validation.constraints.NotNull;

public record ConversationCreateRequest(
        @NotNull(message = "Participant id is required")
        Long participantId
) {
}
