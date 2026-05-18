package com.lingualink.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DictionaryCreateRequest(
        @NotBlank @Size(max = 100) String name
) {
}
