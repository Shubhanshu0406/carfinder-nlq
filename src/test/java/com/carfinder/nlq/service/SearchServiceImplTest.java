package com.carfinder.nlq.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.carfinder.nlq.dto.SearchRequest;
import com.carfinder.nlq.dto.SearchResponse;
import com.carfinder.nlq.dto.VehicleFilter;
import com.carfinder.nlq.dto.VehicleResult;
import com.carfinder.nlq.llm.NlQueryParser;
import com.carfinder.nlq.search.VehicleQueryBuilder;
import com.carfinder.nlq.search.VehicleSearchRepository;
import com.carfinder.nlq.search.VehicleSearchRepository.PagedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private NlQueryParser nlQueryParser;
    @Mock
    private VehicleQueryBuilder queryBuilder;
    @Mock
    private VehicleSearchRepository repository;

    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchServiceImpl(nlQueryParser, queryBuilder, repository);
    }

    @Test
    void orchestratesParseBuildAndSearch() {
        SearchRequest request = new SearchRequest("Show SUVs under 15L", 0, 10);
        VehicleFilter filter = new VehicleFilter(null, 1_500_000L, "suv", null, null, null, null, null, null, null, null, null);
        Query esQuery = Query.of(q -> q.matchAll(m -> m));
        VehicleResult result = new VehicleResult(
                "id-1", "Tata", "Nexon", "VX", 900_000, "suv", "petrol", "manual",
                20_000, 2022, 5, "Pune", java.time.LocalDate.now(), 5.0);

        when(nlQueryParser.parse("Show SUVs under 15L")).thenReturn(filter);
        when(queryBuilder.build(filter)).thenReturn(esQuery);
        when(repository.search(esQuery, 0, 10)).thenReturn(new PagedResult(List.of(result), 1));

        SearchResponse response = searchService.search(request);

        assertThat(response.query()).isEqualTo("Show SUVs under 15L");
        assertThat(response.appliedFilters()).isEqualTo(filter);
        assertThat(response.results()).containsExactly(result);
        assertThat(response.totalHits()).isEqualTo(1);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);

        verify(nlQueryParser).parse("Show SUVs under 15L");
        verify(queryBuilder).build(filter);
        verify(repository).search(esQuery, 0, 10);
    }
}
