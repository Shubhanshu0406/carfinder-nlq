package com.carfinder.nlq.llm;

import java.util.Map;

/** JSON schema constraining Gemini's structured output to the filter shape. */
final class GeminiFilterSchema {

    static final Map<String, Object> SCHEMA = Map.of(
            "type", "OBJECT",
            "properties", Map.ofEntries(
                    Map.entry("price_min", Map.of("type", "INTEGER", "nullable", true)),
                    Map.entry("price_max", Map.of("type", "INTEGER", "nullable", true)),
                    Map.entry("body_type", Map.of("type", "STRING", "nullable", true)),
                    Map.entry("fuel_type", Map.of("type", "STRING", "nullable", true)),
                    Map.entry("transmission", Map.of("type", "STRING", "nullable", true)),
                    Map.entry("km_driven_min", Map.of("type", "INTEGER", "nullable", true)),
                    Map.entry("km_driven_max", Map.of("type", "INTEGER", "nullable", true)),
                    Map.entry("year_min", Map.of("type", "INTEGER", "nullable", true)),
                    Map.entry("year_max", Map.of("type", "INTEGER", "nullable", true)),
                    Map.entry("safety_rating_min", Map.of("type", "INTEGER", "nullable", true)),
                    Map.entry("brand", Map.of("type", "STRING", "nullable", true)),
                    Map.entry("model", Map.of("type", "STRING", "nullable", true))));

    private GeminiFilterSchema() {
    }
}
