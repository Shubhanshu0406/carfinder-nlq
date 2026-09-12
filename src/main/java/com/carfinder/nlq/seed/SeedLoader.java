package com.carfinder.nlq.seed;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.carfinder.nlq.json.VehicleJsonMapper;
import com.carfinder.nlq.model.VehicleDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Creates the ES index from mapping.json and bulk-loads vehicles.ndjson if the index is missing. */
@Component
public class SeedLoader implements CommandLineRunner {

    private final ElasticsearchClient client;
    private final String index;
    private final boolean autoLoadEnabled;
    private final Path mappingFile;
    private final Path seedFile;
    private final ObjectMapper objectMapper = VehicleJsonMapper.create();

    public SeedLoader(
            ElasticsearchClient client,
            @Value("${elasticsearch.index}") String index,
            @Value("${app.seed.auto-load}") boolean autoLoadEnabled,
            @Value("${app.seed.mapping-file}") String mappingFile,
            @Value("${app.seed.file}") String seedFile) {
        this.client = client;
        this.index = index;
        this.autoLoadEnabled = autoLoadEnabled;
        this.mappingFile = Path.of(mappingFile);
        this.seedFile = Path.of(seedFile);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!autoLoadEnabled || indexExists()) {
            return;
        }
        createIndex();
        bulkLoad();
    }

    private boolean indexExists() throws Exception {
        return client.indices().exists(e -> e.index(index)).value();
    }

    private void createIndex() throws Exception {
        String mappingJson = Files.readString(mappingFile);
        client.indices().create(CreateIndexRequest.of(c -> c.index(index).withJson(new StringReader(mappingJson))));
    }

    private void bulkLoad() throws Exception {
        List<String> lines = Files.readAllLines(seedFile);
        List<BulkOperation> operations = lines.stream()
                .filter(line -> !line.isBlank() && !line.contains("\"index\""))
                .map(this::toBulkOperation)
                .toList();

        if (!operations.isEmpty()) {
            client.bulk(BulkRequest.of(b -> b.index(index).operations(operations)));
        }
    }

    private BulkOperation toBulkOperation(String sourceLine) {
        try {
            VehicleDocument doc = objectMapper.readValue(sourceLine, VehicleDocument.class);
            return BulkOperation.of(op -> op.index(idx -> idx.id(doc.id()).document(doc)));
        } catch (Exception ex) {
            throw new IllegalStateException("Malformed seed data line: " + sourceLine, ex);
        }
    }
}
