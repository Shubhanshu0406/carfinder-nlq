package com.carfinder.nlq.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.carfinder.nlq.dto.VehicleResult;
import com.carfinder.nlq.exception.SearchExecutionException;
import com.carfinder.nlq.model.VehicleDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
public class VehicleSearchRepository {

    private final ElasticsearchClient client;
    private final String index;

    public VehicleSearchRepository(ElasticsearchClient client, @Value("${elasticsearch.index}") String index) {
        this.client = client;
        this.index = index;
    }

    public PagedResult search(Query query, int page, int size) {
        try {
            SearchResponse<VehicleDocument> response = client.search(s -> s
                    .index(index)
                    .query(query)
                    .from(page * size)
                    .size(size), VehicleDocument.class);

            List<VehicleResult> results = response.hits().hits().stream()
                    .map(this::toVehicleResult)
                    .toList();
            long totalHits = response.hits().total() != null ? response.hits().total().value() : results.size();

            return new PagedResult(results, totalHits);
        } catch (IOException ex) {
            throw new SearchExecutionException("Failed to execute vehicle search", ex);
        }
    }

    private VehicleResult toVehicleResult(Hit<VehicleDocument> hit) {
        VehicleDocument doc = hit.source();
        double score = hit.score() != null ? hit.score() : 0.0;
        return new VehicleResult(
                doc.id(), doc.brand(), doc.model(), doc.variant(), doc.price(), doc.bodyType(),
                doc.fuelType(), doc.transmission(), doc.kmDriven(), doc.year(), doc.safetyRating(),
                doc.city(), doc.listedDate(), score);
    }

    public record PagedResult(List<VehicleResult> results, long totalHits) {
    }
}
