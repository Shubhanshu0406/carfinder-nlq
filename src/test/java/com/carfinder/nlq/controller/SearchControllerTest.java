package com.carfinder.nlq.controller;

import com.carfinder.nlq.dto.SearchResponse;
import com.carfinder.nlq.dto.VehicleFilter;
import com.carfinder.nlq.exception.LlmParsingException;
import com.carfinder.nlq.exception.SearchExecutionException;
import com.carfinder.nlq.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private SearchService searchService;

    @Test
    void validQueryReturnsSearchResponse() throws Exception {
        VehicleFilter filter = new VehicleFilter(null, 1_500_000L, "suv", null, null, null, null, null, null, null, null, null);
        SearchResponse response = new SearchResponse("Show SUVs under 15L", filter, List.of(), 0, 20, 0);
        when(searchService.search(any())).thenReturn(response);

        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SearchRequestJson("Show SUVs under 15L", 0, 20))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Show SUVs under 15L"))
                .andExpect(jsonPath("$.appliedFilters.bodyType").value("suv"))
                .andExpect(jsonPath("$.totalHits").value(0));
    }

    @Test
    void blankQueryReturns400() throws Exception {
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SearchRequestJson("", 0, 20))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void llmParsingFailureReturns422() throws Exception {
        when(searchService.search(any())).thenThrow(new LlmParsingException("could not parse"));

        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SearchRequestJson("gibberish query", 0, 20))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("could not parse"));
    }

    @Test
    void searchExecutionFailureReturns502() throws Exception {
        when(searchService.search(any())).thenThrow(new SearchExecutionException("es down", new RuntimeException()));

        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SearchRequestJson("SUVs", 0, 20))))
                .andExpect(status().isBadGateway());
    }

    private record SearchRequestJson(String query, int page, int size) {
    }
}
