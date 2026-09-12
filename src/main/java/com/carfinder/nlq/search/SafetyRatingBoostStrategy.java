package com.carfinder.nlq.search;

import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import org.springframework.stereotype.Component;

/** Higher safety_rating scores higher, linearly. */
@Component
public class SafetyRatingBoostStrategy implements ScoringStrategy {

    @Override
    public FunctionScore toFunctionScore() {
        return FunctionScore.of(f -> f.fieldValueFactor(fvf -> fvf
                .field("safety_rating")
                .factor(1.2)
                .modifier(FieldValueFactorModifier.None)
                .missing(1.0)));
    }
}
