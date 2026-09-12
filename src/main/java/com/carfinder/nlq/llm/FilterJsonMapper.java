package com.carfinder.nlq.llm;

import com.carfinder.nlq.dto.VehicleFilter;
import com.carfinder.nlq.exception.LlmParsingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

/** Parses the raw JSON text returned by the LLM into a {@link VehicleFilter}. */
@Component
public class FilterJsonMapper {

    private final ObjectMapper objectMapper;

    public FilterJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public VehicleFilter toFilter(String rawJson) {
        try {
            GeminiFilterPayload payload = objectMapper.readValue(rawJson, GeminiFilterPayload.class);
            return new VehicleFilter(
                    payload.priceMin(),
                    payload.priceMax(),
                    normalize(payload.bodyType()),
                    normalize(payload.fuelType()),
                    normalize(payload.transmission()),
                    payload.kmDrivenMin(),
                    payload.kmDrivenMax(),
                    payload.yearMin(),
                    payload.yearMax(),
                    payload.safetyRatingMin(),
                    payload.brand(),
                    payload.model());
        } catch (Exception ex) {
            throw new LlmParsingException("Could not parse LLM response into a filter: " + rawJson, ex);
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.toLowerCase();
    }
}
