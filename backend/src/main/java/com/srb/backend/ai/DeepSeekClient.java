package com.srb.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DeepSeekClient {

    private static final ParameterizedTypeReference<ServerSentEvent<String>> SSE_TYPE = new ParameterizedTypeReference<>() {};

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${deepseek.api-key}")
    private String apiKey;

    @org.springframework.beans.factory.annotation.Value("${deepseek.base-url}")
    private String baseUrl;

    public Flux<String> streamChat(List<DeepSeekMessage> messages, String model, boolean disableThinking, int maxTokens) {
        Map<String, Object> requestBody = buildRequestBody(messages, model, disableThinking, maxTokens, true);
        return webClientBuilder.build()
                .post()
                .uri(normalizeBaseUrl() + "/v1/chat/completions")
                .headers(headers -> {
                    headers.setBearerAuth(apiKey);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(SSE_TYPE)
                .mapNotNull(ServerSentEvent::data)
                .takeUntil("[DONE]"::equals)
                .filter(data -> !"[DONE]".equals(data))
                .flatMapIterable(this::extractDeltaContents);
    }

    public String chatOnce(List<DeepSeekMessage> messages, String model, boolean disableThinking, int maxTokens) {
        return chatOnce(messages, model, disableThinking, maxTokens, 0.3d);
    }

    public String chatOnce(List<DeepSeekMessage> messages, String model, boolean disableThinking, int maxTokens, double temperature) {
        Map<String, Object> requestBody = buildRequestBody(messages, model, disableThinking, maxTokens, false, temperature);
        JsonNode response = webClientBuilder.build()
                .post()
                .uri(normalizeBaseUrl() + "/v1/chat/completions")
                .headers(headers -> {
                    headers.setBearerAuth(apiKey);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(120));
        return extractMessageContent(response);
    }

    private Map<String, Object> buildRequestBody(List<DeepSeekMessage> messages, String model, boolean disableThinking,
                                                 int maxTokens, boolean stream) {
        return buildRequestBody(messages, model, disableThinking, maxTokens, stream, 0.3d);
    }

    private Map<String, Object> buildRequestBody(List<DeepSeekMessage> messages, String model, boolean disableThinking,
                                                 int maxTokens, boolean stream, double temperature) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", convertMessages(messages));
        body.put("stream", stream);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        if (disableThinking) {
            body.put("thinking", Map.of("type", "disabled"));
        }
        return body;
    }

    private List<Map<String, Object>> convertMessages(List<DeepSeekMessage> messages) {
        List<Map<String, Object>> converted = new ArrayList<>();
        for (DeepSeekMessage message : messages) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("role", message.role());
            item.put("content", message.content());
            converted.add(item);
        }
        return converted;
    }

    private List<String> extractDeltaContents(String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            List<String> chunks = new ArrayList<>();
            JsonNode choices = root.path("choices");
            if (choices.isArray()) {
                for (JsonNode choice : choices) {
                    JsonNode contentNode = choice.path("delta").path("content");
                    if (contentNode.isTextual() && !contentNode.asText().isEmpty()) {
                        chunks.add(contentNode.asText());
                    }
                }
            }
            return chunks;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String extractMessageContent(JsonNode root) {
        if (root == null) {
            return "";
        }
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return "";
        }
        JsonNode contentNode = choices.get(0).path("message").path("content");
        return contentNode.isTextual() ? contentNode.asText("") : "";
    }

    private String normalizeBaseUrl() {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
