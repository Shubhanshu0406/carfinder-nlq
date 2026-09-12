package com.carfinder.nlq.llm;

import com.carfinder.nlq.dto.VehicleFilter;
import com.carfinder.nlq.exception.LlmParsingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Component
public class GeminiNlQueryParser implements NlQueryParser {

    private final RestClient geminiRestClient;
    private final FilterJsonMapper filterJsonMapper;
    private final String model;
    private final String apiKey;
    private final String promptTemplate;

    public GeminiNlQueryParser(
            RestClient geminiRestClient,
            FilterJsonMapper filterJsonMapper,
            @Value("${gemini.model}") String model,
            @Value("${gemini.api-key}") String apiKey,
            @Value("classpath:prompts/nl-to-filter-prompt.txt") Resource promptResource) {
        this.geminiRestClient = geminiRestClient;
        this.filterJsonMapper = filterJsonMapper;
        this.model = model;
        this.apiKey = apiKey;
        this.promptTemplate = readPrompt(promptResource);
    }

    @Override
    public VehicleFilter parse(String naturalLanguageQuery) {
        String prompt = resolvePlaceholders(promptTemplate) + naturalLanguageQuery;
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", GeminiFilterSchema.SCHEMA));

        JsonNode response;
        try {
            response = geminiRestClient.post()
                    .uri("/models/{model}:generateContent?key={key}", model, apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            throw new LlmParsingException("LLM call failed: " + ex.getMessage(), ex);
        }

        String rawJson = extractText(response);
        return filterJsonMapper.toFilter(rawJson);
    }

    private static String resolvePlaceholders(String template) {
        int currentYear = Year.now().getValue();
        return template
                .replace("{{current_year}}", String.valueOf(currentYear))
                .replace("{{current_year_minus_5}}", String.valueOf(currentYear - 5));
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            throw new LlmParsingException("Empty response from LLM");
        }
        JsonNode text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (text.isMissingNode()) {
            throw new LlmParsingException("Unexpected LLM response shape: " + response);
        }
        return text.asText();
    }

    private static String readPrompt(Resource resource) {
        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load NL-to-filter prompt template", ex);
        }
    }
}
