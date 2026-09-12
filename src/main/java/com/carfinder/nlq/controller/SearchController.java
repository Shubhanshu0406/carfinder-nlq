package com.carfinder.nlq.controller;

import com.carfinder.nlq.dto.SearchRequest;
import com.carfinder.nlq.dto.SearchResponse;
import com.carfinder.nlq.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/api/search")
    public SearchResponse search(@Valid @RequestBody SearchRequest request) {
        return searchService.search(request);
    }
}
