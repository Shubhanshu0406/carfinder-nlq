package com.carfinder.nlq.seed;

import com.carfinder.nlq.json.VehicleJsonMapper;
import com.carfinder.nlq.model.VehicleDocument;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Standalone generator: run via `mvn exec:java` to (re)produce seed-data/vehicles.ndjson. */
public final class SeedDataGenerator {

    private static final int DOCUMENT_COUNT = 1200;
    private static final long SEED = 42L;
    private static final Path OUTPUT_PATH = Path.of("seed-data", "vehicles.ndjson");
    private static final String INDEX_NAME = "vehicles";

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = VehicleJsonMapper.create();
        List<VehicleDocument> vehicles = new VehicleFactory(SEED).generate(DOCUMENT_COUNT);

        StringBuilder ndjson = new StringBuilder();
        for (VehicleDocument vehicle : vehicles) {
            Map<String, Object> action = Map.of("index", Map.of("_index", INDEX_NAME, "_id", vehicle.id()));
            ndjson.append(mapper.writeValueAsString(action)).append('\n');
            ndjson.append(mapper.writeValueAsString(vehicle)).append('\n');
        }

        Files.createDirectories(OUTPUT_PATH.getParent());
        Files.writeString(OUTPUT_PATH, ndjson.toString());
        System.out.printf("Wrote %d vehicles to %s%n", vehicles.size(), OUTPUT_PATH);
    }
}
