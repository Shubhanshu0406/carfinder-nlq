package com.carfinder.nlq.search;

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.carfinder.nlq.dto.VehicleFilter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Translates a {@link VehicleFilter} into a bool query wrapped in function_score ranking. */
@Component
public class VehicleQueryBuilder {

    private final List<ScoringStrategy> scoringStrategies;

    public VehicleQueryBuilder(List<ScoringStrategy> scoringStrategies) {
        this.scoringStrategies = scoringStrategies;
    }

    public Query build(VehicleFilter filter) {
        Query boolQuery = buildBoolQuery(filter);
        List<FunctionScore> functions = scoringStrategies.stream().map(ScoringStrategy::toFunctionScore).toList();

        // bool query below is filter-only (no relevance score), so the function_score functions
        // must fully replace the score rather than multiply against a meaningless 0.
        return Query.of(q -> q.functionScore(fs -> fs
                .query(boolQuery)
                .functions(functions)
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Replace)));
    }

    Query buildBoolQuery(VehicleFilter filter) {
        List<Query> filters = new ArrayList<>();
        addPriceRange(filter, filters);
        addBodyType(filter, filters);
        addFuelType(filter, filters);
        addTransmission(filter, filters);
        addKmDrivenRange(filter, filters);
        addYearRange(filter, filters);
        addSafetyRatingMin(filter, filters);
        addBrand(filter, filters);
        addModel(filter, filters);

        return Query.of(q -> q.bool(b -> b.filter(filters)));
    }

    private void addPriceRange(VehicleFilter filter, List<Query> filters) {
        addRange(filters, "price", filter.priceMin(), filter.priceMax());
    }

    private void addKmDrivenRange(VehicleFilter filter, List<Query> filters) {
        addRange(filters, "km_driven", filter.kmDrivenMin(), filter.kmDrivenMax());
    }

    private void addYearRange(VehicleFilter filter, List<Query> filters) {
        Long min = filter.yearMin() == null ? null : filter.yearMin().longValue();
        Long max = filter.yearMax() == null ? null : filter.yearMax().longValue();
        addRange(filters, "year", min, max);
    }

    private void addSafetyRatingMin(VehicleFilter filter, List<Query> filters) {
        if (filter.safetyRatingMin() != null) {
            filters.add(Query.of(q -> q.range(r -> r.untyped(u -> u
                    .field("safety_rating")
                    .gte(JsonData.of(filter.safetyRatingMin()))))));
        }
    }

    private void addRange(List<Query> filters, String field, Long min, Long max) {
        if (min == null && max == null) {
            return;
        }
        filters.add(Query.of(q -> q.range(r -> {
            r.untyped(u -> {
                u.field(field);
                if (min != null) {
                    u.gte(JsonData.of(min));
                }
                if (max != null) {
                    u.lte(JsonData.of(max));
                }
                return u;
            });
            return r;
        })));
    }

    private void addBodyType(VehicleFilter filter, List<Query> filters) {
        addTerm(filters, "body_type", filter.bodyType());
    }

    private void addFuelType(VehicleFilter filter, List<Query> filters) {
        addTerm(filters, "fuel_type", filter.fuelType());
    }

    private void addTransmission(VehicleFilter filter, List<Query> filters) {
        addTerm(filters, "transmission", filter.transmission());
    }

    private void addBrand(VehicleFilter filter, List<Query> filters) {
        addTerm(filters, "brand", filter.brand());
    }

    private void addModel(VehicleFilter filter, List<Query> filters) {
        addTerm(filters, "model", filter.model());
    }

    private void addTerm(List<Query> filters, String field, String value) {
        if (value != null && !value.isBlank()) {
            filters.add(Query.of(q -> q.term(t -> t.field(field).value(value))));
        }
    }
}
