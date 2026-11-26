package com.lingualink.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Component
public class OpenRouterClient {
    private final RestClient client;
    private final String apiKey;
    private final String model;

    public OpenRouterClient(RestClient.Builder builder,
                            @Value("${openrouter.base-url}") String baseUrl,
                            @Value("${openrouter.api-key}") String apiKey,
                            @Value("${openrouter.model}") String model) {
        this.client = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey.trim();
        this.model = model;
    }

    record Message(String role, String content) {}

    String complete(List<Message> messages, int maxTokens) {
        if (apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ключ OpenRouter не настроен.");
        }
        JsonNode response;
        try {
            response = client.post().uri("/chat/completions")
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model", model, "messages", messages, "max_tokens", maxTokens,
                            "reasoning", Map.of("enabled", false)))
                    .retrieve().body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Достигнут лимит запросов OpenRouter. Попробуйте позже.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenRouter не смог обработать запрос.");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Не удалось получить ответ от OpenRouter.");
        }
        if (response == null || response.hasNonNull("error")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenRouter вернул ответ без результата.");
        }
        JsonNode choice = response.at("/choices/0");
        String finishReason = choice.path("finish_reason").asText();
        if ("length".equals(finishReason)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ответ превысил лимит. Попробуйте более короткий текст.");
        }
        String content = choice.at("/message/content").textValue();
        if (!"stop".equals(finishReason) || content == null || content.isBlank() || choice.hasNonNull("error")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenRouter вернул ответ без результата.");
        }
        return content.strip();
    }
}
