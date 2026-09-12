package com.carfinder.nlq.llm;

import com.carfinder.nlq.dto.VehicleFilter;
import com.carfinder.nlq.exception.LlmParsingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeminiNlQueryParserTest {

    @Mock
    private RestClient restClient;
    @Mock
    private FilterJsonMapper filterJsonMapper;

    private RestClient.RequestBodyUriSpec uriSpec;
    private RestClient.RequestBodySpec bodySpec;
    private RestClient.ResponseSpec responseSpec;

    private GeminiNlQueryParser parser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        bodySpec = mock(RestClient.RequestBodySpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(any(String.class), any(Object[].class))).thenReturn(bodySpec);
        when(bodySpec.body(any(Object.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);

        parser = new GeminiNlQueryParser(
                restClient, filterJsonMapper, "gemini-2.0-flash", "test-key",
                new ByteArrayResource("Current year is {{current_year}}. Extract filters for: ".getBytes()));
    }

    private JsonNode responseWithText(String text) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
                {"candidates": [{"content": {"parts": [{"text": %s}]}}]}
                """.formatted(mapper.writeValueAsString(text));
        return mapper.readTree(json);
    }

    @Test
    void extractsTextAndDelegatesToFilterJsonMapper() throws Exception {
        JsonNode response = responseWithText("{\"body_type\": \"suv\"}");
        when(responseSpec.body(JsonNode.class)).thenReturn(response);
        VehicleFilter expected = new VehicleFilter(null, null, "suv", null, null, null, null, null, null, null, null, null);
        when(filterJsonMapper.toFilter("{\"body_type\": \"suv\"}")).thenReturn(expected);

        VehicleFilter result = parser.parse("Show SUVs");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    void resolvesCurrentYearPlaceholderInPrompt() throws Exception {
        JsonNode response = responseWithText("{}");
        when(responseSpec.body(JsonNode.class)).thenReturn(response);
        when(filterJsonMapper.toFilter(any())).thenReturn(
                new VehicleFilter(null, null, null, null, null, null, null, null, null, null, null, null));

        parser.parse("Show newer hatchbacks");

        ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(bodySpec).body(bodyCaptor.capture());
        List<Map<String, Object>> contents = (List<Map<String, Object>>) bodyCaptor.getValue().get("contents");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) contents.get(0).get("parts");
        String prompt = (String) parts.get(0).get("text");

        assertThat(prompt).contains("Current year is " + Year.now().getValue());
        assertThat(prompt).doesNotContain("{{current_year}}");
    }

    @Test
    void nullResponseThrowsLlmParsingException() {
        when(responseSpec.body(JsonNode.class)).thenReturn(null);

        assertThatThrownBy(() -> parser.parse("Show SUVs"))
                .isInstanceOf(LlmParsingException.class)
                .hasMessageContaining("Empty response");
    }

    @Test
    void missingCandidatesShapeThrowsLlmParsingException() throws Exception {
        JsonNode response = new ObjectMapper().readTree("{}");
        when(responseSpec.body(JsonNode.class)).thenReturn(response);

        assertThatThrownBy(() -> parser.parse("Show SUVs"))
                .isInstanceOf(LlmParsingException.class)
                .hasMessageContaining("Unexpected LLM response shape");
    }

    @Test
    void restClientFailureIsWrappedAsLlmParsingException() {
        when(responseSpec.body(JsonNode.class)).thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> parser.parse("Show SUVs"))
                .isInstanceOf(LlmParsingException.class)
                .hasMessageContaining("LLM call failed");
    }
}
