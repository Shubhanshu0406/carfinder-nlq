package com.carfinder.nlq.dto;

/** Structured filter extracted from a natural-language query. Any field may be null (unspecified). */
public record VehicleFilter(
        Long priceMin,
        Long priceMax,
        String bodyType,
        String fuelType,
        String transmission,
        Long kmDrivenMin,
        Long kmDrivenMax,
        Integer yearMin,
        Integer yearMax,
        Integer safetyRatingMin,
        String brand,
        String model) {
}
