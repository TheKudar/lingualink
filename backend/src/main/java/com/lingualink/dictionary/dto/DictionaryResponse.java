package com.lingualink.dictionary.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DictionaryResponse(
        Long id,
        String name,
        List<DictionaryEntryResponse> entries,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
