package com.carfinder.nlq.dto;

import java.util.List;

public record SearchResponse(
        String query,
        VehicleFilter appliedFilters,
        List<VehicleResult> results,
        int page,
        int size,
        long totalHits) {
}
