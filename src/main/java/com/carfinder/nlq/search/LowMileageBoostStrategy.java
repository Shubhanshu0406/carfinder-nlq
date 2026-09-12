package com.carfinder.nlq.search;

import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import org.springframework.stereotype.Component;

/** Lower km_driven scores higher, via a reciprocal factor. */
@Component
public class LowMileageBoostStrategy implements ScoringStrategy {

    @Override
    public FunctionScore toFunctionScore() {
        return FunctionScore.of(f -> f.fieldValueFactor(fvf -> fvf
                .field("km_driven")
                .factor(1.0)
                .modifier(FieldValueFactorModifier.Reciprocal)
                .missing(100_000.0)));
    }
}
