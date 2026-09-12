package com.carfinder.nlq.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.carfinder.nlq.dto.VehicleFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleQueryBuilderTest {

    private final VehicleQueryBuilder queryBuilder =
            new VehicleQueryBuilder(List.of(new RecencyBoostStrategy(), new SafetyRatingBoostStrategy(), new LowMileageBoostStrategy()));

    private static VehicleFilter emptyFilter() {
        return new VehicleFilter(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void wrapsBoolQueryInFunctionScore() {
        Query query = queryBuilder.build(emptyFilter());

        assertThat(query.isFunctionScore()).isTrue();
        FunctionScoreQuery functionScore = query.functionScore();
        assertThat(functionScore.functions()).hasSize(3);
        assertThat(functionScore.boostMode())
                .isEqualTo(co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode.Replace);
    }

    @Test
    void noFiltersProducesEmptyBoolFilterClause() {
        Query boolQuery = queryBuilder.buildBoolQuery(emptyFilter());

        BoolQuery bool = boolQuery.bool();
        assertThat(bool.filter()).isEmpty();
    }

    @Test
    void priceRangeAddsBothBoundsWhenPresent() {
        VehicleFilter filter = new VehicleFilter(
                500_000L, 1_500_000L, null, null, null, null, null, null, null, null, null, null);

        Query boolQuery = queryBuilder.buildBoolQuery(filter);
        List<Query> filters = boolQuery.bool().filter();

        assertThat(filters).hasSize(1);
        RangeQuery range = filters.get(0).range();
        assertThat(range.untyped().field()).isEqualTo("price");
        assertThat(range.untyped().gte().to(Long.class)).isEqualTo(500_000L);
        assertThat(range.untyped().lte().to(Long.class)).isEqualTo(1_500_000L);
    }

    @Test
    void priceMaxOnlyOmitsLowerBound() {
        VehicleFilter filter = new VehicleFilter(
                null, 1_500_000L, null, null, null, null, null, null, null, null, null, null);

        Query boolQuery = queryBuilder.buildBoolQuery(filter);
        RangeQuery range = boolQuery.bool().filter().get(0).range();

        assertThat(range.untyped().gte()).isNull();
        assertThat(range.untyped().lte().to(Long.class)).isEqualTo(1_500_000L);
    }

    @Test
    void bodyTypeAddsTermFilter() {
        VehicleFilter filter = new VehicleFilter(
                null, null, "suv", null, null, null, null, null, null, null, null, null);

        Query boolQuery = queryBuilder.buildBoolQuery(filter);
        TermQuery term = boolQuery.bool().filter().get(0).term();

        assertThat(term.field()).isEqualTo("body_type");
        assertThat(term.value().stringValue()).isEqualTo("suv");
    }

    @Test
    void blankBodyTypeIsIgnored() {
        VehicleFilter filter = new VehicleFilter(
                null, null, "  ", null, null, null, null, null, null, null, null, null);

        Query boolQuery = queryBuilder.buildBoolQuery(filter);

        assertThat(boolQuery.bool().filter()).isEmpty();
    }

    @Test
    void kmDrivenRangeUsesKmDrivenField() {
        VehicleFilter filter = new VehicleFilter(
                null, null, null, null, null, null, 80_000L, null, null, null, null, null);

        Query boolQuery = queryBuilder.buildBoolQuery(filter);
        RangeQuery range = boolQuery.bool().filter().get(0).range();

        assertThat(range.untyped().field()).isEqualTo("km_driven");
        assertThat(range.untyped().lte().to(Long.class)).isEqualTo(80_000L);
    }

    @Test
    void yearRangeUsesYearField() {
        VehicleFilter filter = new VehicleFilter(
                null, null, null, null, null, null, null, 2021, null, null, null, null);

        Query boolQuery = queryBuilder.buildBoolQuery(filter);
        RangeQuery range = boolQuery.bool().filter().get(0).range();

        assertThat(range.untyped().field()).isEqualTo("year");
        assertThat(range.untyped().gte().to(Long.class)).isEqualTo(2021L);
    }

    @Test
    void safetyRatingMinAddsGteRangeFilter() {
        VehicleFilter filter = new VehicleFilter(
                null, null, null, null, null, null, null, null, null, 4, null, null);

        Query boolQuery = queryBuilder.buildBoolQuery(filter);
        RangeQuery range = boolQuery.bool().filter().get(0).range();

        assertThat(range.untyped().field()).isEqualTo("safety_rating");
        assertThat(range.untyped().gte().to(Integer.class)).isEqualTo(4);
    }

    @Test
    void combinesMultipleFilterDimensions() {
        VehicleFilter filter = new VehicleFilter(
                null, 1_500_000L, "suv", "diesel", "automatic", null, 80_000L, 2021, null, 4, "Tata", null);

        Query boolQuery = queryBuilder.buildBoolQuery(filter);

        assertThat(boolQuery.bool().filter()).hasSize(8);
    }

    @Test
    void brandAndModelAddTermFilters() {
        VehicleFilter filter = new VehicleFilter(
                null, null, null, null, null, null, null, null, null, null, "Tata", "Nexon");

        Query boolQuery = queryBuilder.buildBoolQuery(filter);
        List<Query> filters = boolQuery.bool().filter();

        assertThat(filters).hasSize(2);
        assertThat(filters.get(0).term().field()).isEqualTo("brand");
        assertThat(filters.get(1).term().field()).isEqualTo("model");
    }
}
