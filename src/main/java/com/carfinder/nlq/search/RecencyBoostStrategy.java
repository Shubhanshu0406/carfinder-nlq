package com.carfinder.nlq.search;

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.json.JsonData;
import org.springframework.stereotype.Component;

/** Newer listings score higher, decaying with a 1-month half-life. */
@Component
public class RecencyBoostStrategy implements ScoringStrategy {

    @Override
    public FunctionScore toFunctionScore() {
        return FunctionScore.of(f -> f.gauss(g -> g.untyped(u -> u
                .field("listed_date")
                .placement(p -> p
                        .origin(JsonData.of("now"))
                        .scale(JsonData.of("30d"))
                        .decay(0.5)))));
    }
}
