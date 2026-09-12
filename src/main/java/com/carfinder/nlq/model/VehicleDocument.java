package com.carfinder.nlq.model;

import java.time.LocalDate;

public record VehicleDocument(
        String id,
        String brand,
        String model,
        String variant,
        long price,
        String bodyType,
        String fuelType,
        String transmission,
        long kmDriven,
        int year,
        int safetyRating,
        String city,
        LocalDate listedDate) {
}
