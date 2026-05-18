package com.lingualink.dictionary.dto;

import java.time.LocalDateTime;

public record DictionaryEntryResponse(
        Long id,
        String sourceWord,
        String targetWord,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
