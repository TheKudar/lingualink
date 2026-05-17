package com.lingualink.report.dto;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        Long userId,
        Long courseId,
        String message,
        LocalDateTime createdAt
) {
}
