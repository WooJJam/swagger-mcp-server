package com.ndgl.swaggermcp.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.swaggermcp.dto.parser.ParsedEndpointBasicInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndpointBasicInfoParser {

    private final ObjectMapper objectMapper;

    public ParsedEndpointBasicInfo parseBasicInfo(final String path, final String method, final JsonNode operation) {
        final String operationId = operation.path("operationId").asText("");
        final String summary = operation.path("summary").asText("");
        final String description = operation.path("description").asText("");

        final List<String> tags = new ArrayList<>();
        final JsonNode tagsNode = operation.path("tags");
        if (tagsNode.isArray()) {
            tagsNode.forEach(tag -> tags.add(tag.asText()));
        }

        final String tagsJson = convertTagsToJson(tags);

        return new ParsedEndpointBasicInfo(
                path,
                method,
                operationId,
                summary,
                description,
                tagsJson,
                tags
        );
    }

    private String convertTagsToJson(final List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            log.error("Tags를 JSON으로 변환 실패", e);
            return "[]";
        }
    }
}
