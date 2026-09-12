package com.carfinder.nlq.seed;

import com.carfinder.nlq.model.VehicleDocument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** Generates randomized, realistic {@link VehicleDocument}s from the static catalog. */
public final class VehicleFactory {

    private static final List<String> TRIMS = List.of("Base", "Mid", "VX", "ZX", "Top", "Sport");
    private static final int CURRENT_YEAR = LocalDate.now().getYear();

    private final Random random;

    public VehicleFactory(long seed) {
        this.random = new Random(seed);
    }

    public List<VehicleDocument> generate(int count) {
        List<VehicleDocument> vehicles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            vehicles.add(generateOne());
        }
        return vehicles;
    }

    private VehicleDocument generateOne() {
        ModelSpec spec = VehicleCatalog.MODELS.get(random.nextInt(VehicleCatalog.MODELS.size()));
        int age = random.nextInt(10) + 1;
        int year = CURRENT_YEAR - age;
        long kmDriven = (long) age * (8_000 + random.nextInt(9_000));
        // depreciate ~8%/year off base price, plus small noise, floored so old cars stay non-trivial
        double depreciation = Math.pow(0.92, age);
        long price = Math.round(spec.basePrice() * depreciation * (0.9 + random.nextDouble() * 0.2));

        String fuelType = spec.fuelTypes().get(random.nextInt(spec.fuelTypes().size()));
        String transmission = spec.transmissions().get(random.nextInt(spec.transmissions().size()));
        String trim = TRIMS.get(random.nextInt(TRIMS.size()));
        String variant = trim + " " + capitalize(fuelType) + " " + (transmission.equals("automatic") ? "AT" : "MT");

        int safetyRating = clamp(spec.baseSafetyRating() + random.nextInt(3) - 1, 1, 5);
        String city = VehicleCatalog.CITIES.get(random.nextInt(VehicleCatalog.CITIES.size()));
        LocalDate listedDate = LocalDate.now().minusDays(random.nextInt(180));

        return new VehicleDocument(
                UUID.randomUUID().toString(),
                spec.brand(),
                spec.model(),
                variant,
                Math.max(price, 100_000),
                spec.bodyType(),
                fuelType,
                transmission,
                kmDriven,
                year,
                safetyRating,
                city,
                listedDate);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
