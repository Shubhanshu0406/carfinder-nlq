package com.carfinder.nlq.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.carfinder.nlq.dto.SearchRequest;
import com.carfinder.nlq.dto.SearchResponse;
import com.carfinder.nlq.dto.VehicleFilter;
import com.carfinder.nlq.llm.NlQueryParser;
import com.carfinder.nlq.search.VehicleQueryBuilder;
import com.carfinder.nlq.search.VehicleSearchRepository;
import com.carfinder.nlq.search.VehicleSearchRepository.PagedResult;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

    private final NlQueryParser nlQueryParser;
    private final VehicleQueryBuilder queryBuilder;
    private final VehicleSearchRepository repository;

    public SearchServiceImpl(NlQueryParser nlQueryParser, VehicleQueryBuilder queryBuilder, VehicleSearchRepository repository) {
        this.nlQueryParser = nlQueryParser;
        this.queryBuilder = queryBuilder;
        this.repository = repository;
    }

    @Override
    public SearchResponse search(SearchRequest request) {
        VehicleFilter filter = nlQueryParser.parse(request.query());
        Query query = queryBuilder.build(filter);
        PagedResult paged = repository.search(query, request.page(), request.size());

        return new SearchResponse(request.query(), filter, paged.results(), request.page(), request.size(), paged.totalHits());
    }
}
