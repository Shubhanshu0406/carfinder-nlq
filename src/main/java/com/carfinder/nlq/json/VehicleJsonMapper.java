package com.carfinder.nlq.json;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** One shared ObjectMapper config so VehicleDocument (de)serializes as snake_case, matching the ES mapping. */
public final class VehicleJsonMapper {

    private VehicleJsonMapper() {
    }

    public static ObjectMapper create() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    }
}
