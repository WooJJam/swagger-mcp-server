package com.ndgl.swaggermcp.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonSchemaParsingSupport {

    private final ObjectMapper objectMapper;

    public JsonNode selectContentNode(final JsonNode content) {
        if (content == null || content.isMissingNode() || !content.isObject()) {
            return MissingNode.getInstance();
        }

        final JsonNode jsonContent = content.path("application/json");
        if (!jsonContent.isMissingNode()) {
            return jsonContent;
        }

        final Iterator<String> contentTypes = content.fieldNames();
        if (!contentTypes.hasNext()) {
            return MissingNode.getInstance();
        }

        final String firstContentType = contentTypes.next();
        return content.path(firstContentType);
    }

    public ExampleData extractExample(final JsonNode jsonContent) {
        if (jsonContent == null || jsonContent.isMissingNode()) {
            return ExampleData.empty();
        }

        final JsonNode example = jsonContent.path("example");
        if (!example.isMissingNode()) {
            return new ExampleData(example, null, null);
        }

        final JsonNode examples = jsonContent.path("examples");
        if (!examples.isObject()) {
            return ExampleData.empty();
        }

        final Iterator<String> exampleNames = examples.fieldNames();
        if (!exampleNames.hasNext()) {
            return ExampleData.empty();
        }

        final String firstName = exampleNames.next();
        final JsonNode exampleEntry = examples.path(firstName);
        final JsonNode value = exampleEntry.path("value");
        final JsonNode valueNode = value.isMissingNode() ? exampleEntry : value;

        return new ExampleData(
                valueNode,
                firstName,
                exampleEntry.path("description").asText("")
        );
    }

    public String convertToJsonString(final JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("JsonNode를 문자열로 변환 실패", e);
            return null;
        }
    }

    public String extractDtoNameFromRef(final String ref) {
        if (ref == null || ref.isEmpty()) {
            return "";
        }

        final int lastSlashIndex = ref.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < ref.length() - 1) {
            return ref.substring(lastSlashIndex + 1);
        }

        return ref;
    }

    /**
     * $ref를 resolve하여 실제 schema 반환
     *
     * @param swaggerJson 전체 Swagger JSON
     * @param schemaRef $ref 값 (예: #/components/schemas/CreateUserTravelRequest)
     * @return 실제 schema 또는 null
     */
    public JsonNode resolveSchemaRef(final JsonNode swaggerJson, final String schemaRef) {
        if (schemaRef == null || schemaRef.isEmpty() || !schemaRef.startsWith("#/")) {
            return null;
        }

        // #/components/schemas/CreateUserTravelRequest → components/schemas/CreateUserTravelRequest
        final String path = schemaRef.substring(2); // #/ 제거
        final String[] parts = path.split("/");

        JsonNode current = swaggerJson;
        for (final String part : parts) {
            if (current == null || current.isMissingNode()) {
                log.warn("Schema ref를 resolve할 수 없습니다: {}", schemaRef);
                return null;
            }
            current = current.path(part);
        }

        if (current.isMissingNode()) {
            log.warn("Schema를 찾을 수 없습니다: {}", schemaRef);
            return null;
        }

        return current;
    }

    public record ExampleData(JsonNode value, String name, String description) {
        private static ExampleData empty() {
            return new ExampleData(null, null, null);
        }
    }
}

