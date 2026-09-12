package com.carfinder.nlq.llm;

import com.carfinder.nlq.dto.VehicleFilter;

/** Turns a natural-language search query into a structured {@link VehicleFilter}. */
public interface NlQueryParser {

    VehicleFilter parse(String naturalLanguageQuery);
}
