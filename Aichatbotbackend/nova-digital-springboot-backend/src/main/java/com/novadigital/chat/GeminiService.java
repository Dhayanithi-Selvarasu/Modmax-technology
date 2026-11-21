
package com.novadigital.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log =
            LoggerFactory.getLogger(GeminiService.class);

    private static final int MAX_ATTEMPTS = 3;

    private static final String SYSTEM_INSTRUCTION = """
            You are ModMax AI Assistant, the official assistant of ModMax Technology.

            Answer questions about the company, its services, projects,
            technologies and contact details.

            Be friendly, professional, concise and truthful.
            Do not invent facts.

            If asked unrelated questions, politely explain that
            you mainly help with  ModMax Technology.

            Company: ModMax Technology.
            Industry: Software Development.

            Services:
            - Web Development
            - App Development
            - AI Integration
            - Cloud Solutions

            Technologies:
            - React
            - Java
            - Spring Boot
            - Python
            - MySQL
            - JavaScript
            - Docker
            - AWS

            Contact email:Modmaxtechnologies@gmail.com.
            Website: https://example.com.
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiService(
            ObjectMapper mapper,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model) {

        this.apiKey = apiKey;
        this.model = model;

        // HTTP Client configuration
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        // Request timeout configuration
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(60));

        // RestClient configuration
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(requestFactory)
                .build();

        log.info("GeminiService initialized. Model: {}", model);
    }

    public String reply(ChatRequest request) {

        // Validate API key
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Gemini API key is missing.");

            throw new IllegalStateException(
                    "Gemini API key is not configured."
            );
        }

        // Validate request
        if (request == null
                || request.message() == null
                || request.message().isBlank()) {

            throw new IllegalArgumentException(
                    "Chat message cannot be empty."
            );
        }

        // Build conversation contents
        List<Map<String, Object>> contents = new ArrayList<>();

        // Add conversation history
        if (request.history() != null) {

            for (ChatRequest.HistoryItem item : request.history()) {

                if (item == null
                        || item.content() == null
                        || item.content().isBlank()) {
                    continue;
                }

                // Gemini uses "model" instead of "assistant"
                String role = "assistant".equalsIgnoreCase(item.role())
                        ? "model"
                        : "user";

                contents.add(
                        Map.of(
                                "role", role,
                                "parts", List.of(
                                        Map.of("text", item.content())
                                )
                        )
                );
            }
        }

        // Add current user message
        contents.add(
                Map.of(
                        "role", "user",
                        "parts", List.of(
                                Map.of("text", request.message())
                        )
                )
        );

        // Build request body
        Map<String, Object> body = Map.of(
                "systemInstruction",
                Map.of(
                        "parts", List.of(
                                Map.of("text", SYSTEM_INSTRUCTION)
                        )
                ),

                "contents", contents,

                "generationConfig",
                Map.of(
                        "maxOutputTokens", 400,
                        "temperature", 0.7
                )
        );

        // API retry mechanism
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            try {

                log.info(
                        "Sending Gemini request. Model: {} | Attempt: {}/{}",
                        model,
                        attempt,
                        MAX_ATTEMPTS
                );

                // Send request to Gemini API
                JsonNode response = restClient.post()
                        .uri(
                                "/v1beta/models/{model}:generateContent",
                                model
                        )
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);

                // Extract response text
                String reply = extractResponse(response);

                if (reply.isBlank()) {

                    log.error("Gemini returned an empty response.");

                    throw new IllegalStateException(
                            "Gemini returned an empty response."
                    );
                }

                log.info("Gemini response generated successfully.");

                return reply;

            } catch (RestClientResponseException e) {

                int status = e.getStatusCode().value();

                String errorBody = e.getResponseBodyAsString();

                log.error(
                        "Gemini HTTP error. Status: {} | Attempt: {} | Response: {}",
                        status,
                        attempt,
                        errorBody
                );

                // Retry temporary API errors
                if (isRetryableStatus(status)
                        && attempt < MAX_ATTEMPTS) {

                    waitBeforeRetry(attempt);
                    continue;
                }

                throw new IllegalStateException(
                        "Gemini API error: HTTP " + status
                );

            } catch (ResourceAccessException e) {

                log.error(
                        "Gemini connection/timeout error. Attempt: {}",
                        attempt,
                        e
                );

                // Retry connection errors
                if (attempt < MAX_ATTEMPTS) {

                    waitBeforeRetry(attempt);
                    continue;
                }

                throw new IllegalStateException(
                        "Gemini is not responding. Please try again later."
                );

            } catch (IllegalStateException e) {

                // Do not retry application errors
                throw e;

            } catch (Exception e) {

                log.error("Unexpected Gemini error.", e);

                throw new IllegalStateException(
                        "Unable to generate AI response."
                );
            }
        }

        throw new IllegalStateException(
                "Gemini is temporarily unavailable. Please try again later."
        );
    }

    // Extract response text safely
    private String extractResponse(JsonNode response) {

        if (response == null) {
            return "";
        }

        JsonNode candidates = response.path("candidates");

        if (!candidates.isArray() || candidates.isEmpty()) {

            log.warn(
                    "Gemini response contains no candidates. Response: {}",
                    response
            );

            return "";
        }

        JsonNode parts = candidates
                .path(0)
                .path("content")
                .path("parts");

        if (!parts.isArray()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (JsonNode part : parts) {

            String text = part.path("text").asText("");

            if (!text.isBlank()) {
                result.append(text);
            }
        }

        return result.toString().trim();
    }

    // Retry only temporary HTTP errors
    private boolean isRetryableStatus(int status) {

        return status == 429
                || status == 500
                || status == 502
                || status == 503
                || status == 504;
    }

    // Retry delay: 1 second, then 2 seconds
    private void waitBeforeRetry(int attempt) {

        long delay = attempt * 1000L;

        log.warn(
                "Retrying Gemini request after {} ms...",
                delay
        );

        try {

            Thread.sleep(delay);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Gemini retry interrupted."
            );
        }
    }
}
