package com.ndgl.swaggermcp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.swaggermcp.dto.ParsedApiEndpoint;
import com.ndgl.swaggermcp.dto.ParsedTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * OpenAPI 3.0 Swagger JSON 파서
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SwaggerParserService {

    private final ObjectMapper objectMapper;

    /**
     * Swagger JSON에서 모든 API 엔드포인트 추출
     *
     * @param swaggerJson Swagger JSON
     * @return 파싱된 API 엔드포인트 리스트
     */
    public List<ParsedApiEndpoint> parseApiEndpoints(JsonNode swaggerJson) {
        log.info("Parsing API endpoints from Swagger JSON");
        List<ParsedApiEndpoint> endpoints = new ArrayList<>();

        JsonNode paths = swaggerJson.path("paths");
        if (paths.isMissingNode()) {
            log.warn("No 'paths' field found in Swagger JSON");
            return endpoints;
        }

        // 각 path 순회
        Iterator<Map.Entry<String, JsonNode>> pathsIterator = paths.fields();
        while (pathsIterator.hasNext()) {
            Map.Entry<String, JsonNode> pathEntry = pathsIterator.next();
            String path = pathEntry.getKey();
            JsonNode methods = pathEntry.getValue();

            // 각 HTTP 메서드 순회
            Iterator<Map.Entry<String, JsonNode>> methodsIterator = methods.fields();
            while (methodsIterator.hasNext()) {
                Map.Entry<String, JsonNode> methodEntry = methodsIterator.next();
                String method = methodEntry.getKey().toUpperCase();
                JsonNode operation = methodEntry.getValue();

                // Operation 정보 추출
                ParsedApiEndpoint endpoint = parseOperation(path, method, operation);
                endpoints.add(endpoint);

                log.debug("Parsed endpoint: {} {}", method, path);
            }
        }

        log.info("Successfully parsed {} API endpoints", endpoints.size());
        return endpoints;
    }

    /**
     * Operation에서 API 엔드포인트 정보 추출
     *
     * @param path API 경로
     * @param method HTTP 메서드
     * @param operation Operation 노드
     * @return 파싱된 API 엔드포인트
     */
    private ParsedApiEndpoint parseOperation(String path, String method, JsonNode operation) {
        String operationId = operation.path("operationId").asText("");
        String summary = operation.path("summary").asText("");
        String description = operation.path("description").asText("");

        // Tags 추출
        List<String> tags = new ArrayList<>();
        JsonNode tagsNode = operation.path("tags");
        if (tagsNode.isArray()) {
            tagsNode.forEach(tag -> tags.add(tag.asText()));
        }

        // Tags를 JSON 문자열로 변환
        String tagsJson = convertTagsToJson(tags);

        return ParsedApiEndpoint.builder()
                .path(path)
                .method(method)
                .operationId(operationId)
                .summary(summary)
                .description(description)
                .tags(tags)
                .tagsJson(tagsJson)
                .build();
    }

    /**
     * Swagger JSON에서 Tags 정보 추출
     *
     * @param swaggerJson Swagger JSON
     * @return 파싱된 Tag 리스트
     */
    public List<ParsedTag> parseTags(JsonNode swaggerJson) {
        log.info("Parsing tags from Swagger JSON");
        List<ParsedTag> tags = new ArrayList<>();

        JsonNode tagsNode = swaggerJson.path("tags");
        if (tagsNode.isMissingNode() || !tagsNode.isArray()) {
            log.warn("No 'tags' field found in Swagger JSON");
            return tags;
        }

        // 각 tag 순회
        tagsNode.forEach(tagNode -> {
            String name = tagNode.path("name").asText("");
            String description = tagNode.path("description").asText("");

            if (!name.isEmpty()) {
                ParsedTag tag = ParsedTag.builder()
                        .name(name)
                        .description(description)
                        .build();
                tags.add(tag);
                log.debug("Parsed tag: {}", name);
            }
        });

        log.info("Successfully parsed {} tags", tags.size());
        return tags;
    }

    /**
     * Tags 리스트를 JSON 문자열로 변환
     *
     * @param tags Tags 리스트
     * @return JSON 문자열 (예: ["Travel", "User"])
     */
    public String convertTagsToJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert tags to JSON", e);
            return "[]";
        }
    }

    /**
     * Swagger JSON 전체 파싱 (엔드포인트 + Tags)
     *
     * @param swaggerJson Swagger JSON
     * @return 파싱 결과 맵
     */
    public SwaggerParseResult parseSwaggerJson(JsonNode swaggerJson) {
        log.info("Starting full Swagger JSON parsing");

        List<ParsedApiEndpoint> endpoints = parseApiEndpoints(swaggerJson);
        List<ParsedTag> tags = parseTags(swaggerJson);

        SwaggerParseResult result = SwaggerParseResult.builder()
                .endpoints(endpoints)
                .tags(tags)
                .totalEndpoints(endpoints.size())
                .totalTags(tags.size())
                .build();

        log.info("Swagger parsing completed: {} endpoints, {} tags",
                endpoints.size(), tags.size());

        return result;
    }

    /**
     * 파싱 결과 DTO
     */
    @lombok.Getter
    @lombok.Builder
    public static class SwaggerParseResult {
        private List<ParsedApiEndpoint> endpoints;
        private List<ParsedTag> tags;
        private int totalEndpoints;
        private int totalTags;
    }
}
