package com.carfinder.nlq.service;

import com.carfinder.nlq.dto.SearchRequest;
import com.carfinder.nlq.dto.SearchResponse;

public interface SearchService {

    SearchResponse search(SearchRequest request);
}
