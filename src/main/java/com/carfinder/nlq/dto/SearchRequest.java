package com.carfinder.nlq.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
        @NotBlank String query,
        @Min(0) int page,
        @Min(1) @Max(100) int size) {

    public SearchRequest {
        if (size == 0) {
            size = 20;
        }
    }
}
