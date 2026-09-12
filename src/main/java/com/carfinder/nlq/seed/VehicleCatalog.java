package com.carfinder.nlq.seed;

import java.util.List;

/** Static reference data for realistic Indian used-car listings. */
final class VehicleCatalog {

    static final List<String> CITIES = List.of(
            "Delhi", "Mumbai", "Bangalore", "Pune", "Hyderabad",
            "Chennai", "Kolkata", "Ahmedabad", "Jaipur", "Chandigarh");

    static final List<ModelSpec> MODELS = List.of(
            new ModelSpec("Maruti Suzuki", "Swift", "hatchback", 650_000, List.of("petrol", "cng"), List.of("manual", "automatic"), 2),
            new ModelSpec("Maruti Suzuki", "Baleno", "hatchback", 700_000, List.of("petrol"), List.of("manual", "automatic"), 2),
            new ModelSpec("Maruti Suzuki", "Dzire", "sedan", 750_000, List.of("petrol", "cng"), List.of("manual", "automatic"), 2),
            new ModelSpec("Maruti Suzuki", "Brezza", "suv", 950_000, List.of("petrol"), List.of("manual", "automatic"), 4),
            new ModelSpec("Maruti Suzuki", "Ertiga", "muv", 900_000, List.of("petrol", "cng"), List.of("manual", "automatic"), 3),
            new ModelSpec("Maruti Suzuki", "WagonR", "hatchback", 550_000, List.of("petrol", "cng"), List.of("manual"), 2),
            new ModelSpec("Hyundai", "i20", "hatchback", 800_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 3),
            new ModelSpec("Hyundai", "Venue", "suv", 950_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 4),
            new ModelSpec("Hyundai", "Creta", "suv", 1_400_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 5),
            new ModelSpec("Hyundai", "Verna", "sedan", 1_150_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 4),
            new ModelSpec("Hyundai", "Alcazar", "suv", 1_700_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 4),
            new ModelSpec("Tata", "Nexon", "suv", 950_000, List.of("petrol", "diesel", "electric"), List.of("manual", "automatic"), 5),
            new ModelSpec("Tata", "Punch", "suv", 750_000, List.of("petrol"), List.of("manual", "automatic"), 5),
            new ModelSpec("Tata", "Altroz", "hatchback", 700_000, List.of("petrol", "diesel"), List.of("manual"), 5),
            new ModelSpec("Tata", "Harrier", "suv", 1_600_000, List.of("diesel"), List.of("manual", "automatic"), 5),
            new ModelSpec("Tata", "Tiago", "hatchback", 550_000, List.of("petrol", "cng"), List.of("manual"), 4),
            new ModelSpec("Mahindra", "XUV700", "suv", 1_600_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 5),
            new ModelSpec("Mahindra", "Scorpio", "suv", 1_400_000, List.of("diesel"), List.of("manual", "automatic"), 4),
            new ModelSpec("Mahindra", "Thar", "suv", 1_500_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 4),
            new ModelSpec("Mahindra", "Bolero", "suv", 1_000_000, List.of("diesel"), List.of("manual"), 3),
            new ModelSpec("Honda", "City", "sedan", 1_150_000, List.of("petrol"), List.of("manual", "automatic"), 4),
            new ModelSpec("Honda", "Amaze", "sedan", 750_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 4),
            new ModelSpec("Honda", "Elevate", "suv", 1_300_000, List.of("petrol"), List.of("manual", "automatic"), 4),
            new ModelSpec("Toyota", "Innova Crysta", "muv", 1_900_000, List.of("diesel", "petrol"), List.of("manual", "automatic"), 4),
            new ModelSpec("Toyota", "Fortuner", "suv", 3_400_000, List.of("diesel", "petrol"), List.of("manual", "automatic"), 5),
            new ModelSpec("Toyota", "Glanza", "hatchback", 700_000, List.of("petrol"), List.of("manual", "automatic"), 3),
            new ModelSpec("Kia", "Seltos", "suv", 1_150_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 5),
            new ModelSpec("Kia", "Sonet", "suv", 900_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 4),
            new ModelSpec("Kia", "Carens", "muv", 1_100_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 4),
            new ModelSpec("MG", "Hector", "suv", 1_600_000, List.of("petrol", "diesel"), List.of("manual", "automatic"), 4),
            new ModelSpec("MG", "Astor", "suv", 1_400_000, List.of("petrol"), List.of("manual", "automatic"), 4),
            new ModelSpec("MG", "ZS EV", "suv", 2_200_000, List.of("electric"), List.of("automatic"), 4),
            new ModelSpec("Volkswagen", "Taigun", "suv", 1_250_000, List.of("petrol"), List.of("manual", "automatic"), 5),
            new ModelSpec("Volkswagen", "Virtus", "sedan", 1_200_000, List.of("petrol"), List.of("manual", "automatic"), 5),
            new ModelSpec("Skoda", "Slavia", "sedan", 1_250_000, List.of("petrol"), List.of("manual", "automatic"), 5),
            new ModelSpec("Skoda", "Kushaq", "suv", 1_300_000, List.of("petrol"), List.of("manual", "automatic"), 5));

    private VehicleCatalog() {
    }
}
