package com.lingualink.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @NotNull Long courseId,
        @NotBlank @Size(max = 2000) String message
) {
}
