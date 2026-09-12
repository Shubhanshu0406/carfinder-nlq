package com.carfinder.nlq.search;

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;

/** One ranking signal contributed to the function_score query. */
public interface ScoringStrategy {

    FunctionScore toFunctionScore();
}
