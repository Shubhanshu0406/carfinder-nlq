package com.carfinder.nlq.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Mirrors the JSON shape Gemini is instructed to return for a parsed query. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiFilterPayload(
        @JsonProperty("price_min") Long priceMin,
        @JsonProperty("price_max") Long priceMax,
        @JsonProperty("body_type") String bodyType,
        @JsonProperty("fuel_type") String fuelType,
        String transmission,
        @JsonProperty("km_driven_min") Long kmDrivenMin,
        @JsonProperty("km_driven_max") Long kmDrivenMax,
        @JsonProperty("year_min") Integer yearMin,
        @JsonProperty("year_max") Integer yearMax,
        @JsonProperty("safety_rating_min") Integer safetyRatingMin,
        String brand,
        String model) {
}
