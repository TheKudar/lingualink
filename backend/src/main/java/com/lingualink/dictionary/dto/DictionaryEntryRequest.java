package com.lingualink.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DictionaryEntryRequest(
        @NotBlank @Size(max = 200) String sourceWord,
        @NotBlank @Size(max = 200) String targetWord
) {
}
