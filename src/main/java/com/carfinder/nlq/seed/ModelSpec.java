package com.carfinder.nlq.seed;

import java.util.List;

record ModelSpec(
        String brand,
        String model,
        String bodyType,
        long basePrice,
        List<String> fuelTypes,
        List<String> transmissions,
        int baseSafetyRating) {
}
