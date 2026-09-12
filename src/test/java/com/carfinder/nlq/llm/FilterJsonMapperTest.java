package com.carfinder.nlq.llm;

import com.carfinder.nlq.dto.VehicleFilter;
import com.carfinder.nlq.exception.LlmParsingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterJsonMapperTest {

    private final FilterJsonMapper mapper = new FilterJsonMapper(new ObjectMapper());

    @Test
    void mapsFullPayloadToFilter() {
        String json = """
                {"price_max": 1500000, "body_type": "SUV", "fuel_type": "diesel",
                 "transmission": "automatic", "km_driven_max": 80000, "year_min": 2021,
                 "safety_rating_min": 4, "brand": "Tata", "model": "Nexon"}
                """;

        VehicleFilter filter = mapper.toFilter(json);

        assertThat(filter.priceMax()).isEqualTo(1_500_000L);
        assertThat(filter.bodyType()).isEqualTo("suv");
        assertThat(filter.fuelType()).isEqualTo("diesel");
        assertThat(filter.transmission()).isEqualTo("automatic");
        assertThat(filter.kmDrivenMax()).isEqualTo(80_000L);
        assertThat(filter.yearMin()).isEqualTo(2021);
        assertThat(filter.safetyRatingMin()).isEqualTo(4);
        assertThat(filter.brand()).isEqualTo("Tata");
        assertThat(filter.model()).isEqualTo("Nexon");
    }

    @Test
    void missingFieldsMapToNull() {
        VehicleFilter filter = mapper.toFilter("{}");

        assertThat(filter.priceMin()).isNull();
        assertThat(filter.priceMax()).isNull();
        assertThat(filter.bodyType()).isNull();
        assertThat(filter.yearMin()).isNull();
        assertThat(filter.yearMax()).isNull();
        assertThat(filter.safetyRatingMin()).isNull();
    }

    @Test
    void normalizesBodyAndFuelTypeCaseToLowercase() {
        VehicleFilter filter = mapper.toFilter("{\"body_type\": \"SUV\", \"fuel_type\": \"DIESEL\"}");

        assertThat(filter.bodyType()).isEqualTo("suv");
        assertThat(filter.fuelType()).isEqualTo("diesel");
    }

    @Test
    void malformedJsonThrowsLlmParsingException() {
        assertThatThrownBy(() -> mapper.toFilter("not json"))
                .isInstanceOf(LlmParsingException.class);
    }

    @Test
    void blankResponseThrowsLlmParsingException() {
        assertThatThrownBy(() -> mapper.toFilter(""))
                .isInstanceOf(LlmParsingException.class);
    }
}
