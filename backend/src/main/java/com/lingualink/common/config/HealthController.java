package com.lingualink.common.config;

import com.lingualink.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health")
public class HealthController {

    @GetMapping("/api/health")
    @Operation(summary = "Check service health", description = "Public endpoint used to confirm that the backend is running.")
    public ApiResponse<String> health() {
        return ApiResponse.ok("LinguaLink backend is running");
    }
}
