package com.ndgl.swaggermcp.sync.application.service;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwaggerFetchService {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    /**
     * Swagger URL에서 JSON 다운로드
     *
     * @param swaggerUrl Swagger JSON URL
     * @return Swagger JSON (JsonNode)
     */
    public JsonNode fetchSwaggerJson(final String swaggerUrl) {
        log.info("Fetching Swagger JSON from: {}", swaggerUrl);

        try {
            String jsonString = restClient.get()
                    .uri(swaggerUrl)
                    .retrieve()
                    .body(String.class);

            if (jsonString == null || jsonString.isBlank()) {
                throw new IllegalStateException("Swagger JSON is empty from URL: " + swaggerUrl);
            }

            JsonNode jsonNode = objectMapper.readTree(jsonString);
            log.info("Successfully fetched Swagger JSON. Version: {}",
                    jsonNode.path("openapi").asText("unknown"));

            Iterator<String> fieldNames = jsonNode.path("paths").fieldNames();
            while (fieldNames.hasNext()) {
                String path = fieldNames.next();
                log.info("path = {}", path);
            }

            return jsonNode;

        } catch (RestClientException e) {
            log.error("Failed to fetch Swagger JSON from URL: {}", swaggerUrl, e);
            throw new RuntimeException("Failed to fetch Swagger JSON: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to parse Swagger JSON", e);
            throw new RuntimeException("Failed to parse Swagger JSON: " + e.getMessage(), e);
        }
    }

}
