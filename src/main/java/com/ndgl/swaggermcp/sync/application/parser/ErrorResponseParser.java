package com.ndgl.swaggermcp.sync.application.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.ndgl.swaggermcp.sync.dto.ParsedErrorResponse;
import com.ndgl.swaggermcp.sync.support.JsonSchemaParsingSupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorResponseParser {

    private final JsonSchemaParsingSupport parsingSupport;

    public List<ParsedErrorResponse> parseErrorResponses(final JsonNode swaggerJson, final JsonNode operation) {
        final List<ParsedErrorResponse> errors = new ArrayList<>();
        final JsonNode responsesNode = operation.path("responses");

        if (responsesNode.isMissingNode()) {
            return errors;
        }

        for (final Map.Entry<String, JsonNode> entry : responsesNode.properties()) {
            final String statusCodeStr = entry.getKey();
            final JsonNode response = entry.getValue();

            try {
                final int statusCode = Integer.parseInt(statusCodeStr);
                if (statusCode >= 400) {
                    final ParsedErrorResponse errorResponse = parseErrorResponse(swaggerJson, statusCode, response);
                    if (errorResponse != null) {
                        errors.add(errorResponse);
                    }
                }
            } catch (NumberFormatException e) {
                log.debug("숫자가 아닌 상태 코드 건너뜀: {}", statusCodeStr);
            }
        }

        return errors;
    }

    private ParsedErrorResponse parseErrorResponse(final JsonNode swaggerJson, final int statusCode, final JsonNode response) {
        final String description = response.path("description").asText("");
        final JsonNode content = response.path("content");

        if (content.isMissingNode()) {
            return new ParsedErrorResponse(
                    statusCode,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    description,
                    null
            );
        }

        final JsonNode jsonContent = parsingSupport.selectContentNode(content);
        if (jsonContent.isMissingNode()) {
            return new ParsedErrorResponse(
                    statusCode,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    description,
                    null
            );
        }

        final JsonNode schema = jsonContent.path("schema");
        final String schemaRef = schema.path("$ref").asText("");

        // $ref를 resolve하여 실제 schema 가져오기
        final JsonNode resolvedSchema = schemaRef.isEmpty()
                ? schema
                : parsingSupport.resolveSchemaRef(swaggerJson, schemaRef);

        // required 정보를 각 필드에 포함시킨 enriched schema 생성
        final JsonNode enrichedSchema = parsingSupport.enrichSchemaWithRequired(
                resolvedSchema != null ? resolvedSchema : schema
        );

        final JsonSchemaParsingSupport.ExampleData exampleData = parsingSupport.extractExample(jsonContent, resolvedSchema);
        final JsonNode example = exampleData.value() == null ? MissingNode.getInstance() : exampleData.value();
        final String errorCode = firstNonEmpty(
                example.path("errorCode").asText(""),
                example.path("code").asText("")
        );
        final String errorName = firstNonEmpty(
                example.path("errorName").asText(""),
                example.path("name").asText(""),
                exampleData.description(),
                exampleData.name()
        );
        final String message = example.path("message").asText("");

        final String[] errorCodeParts = parseErrorCode(errorCode);

        final String schemaJson = parsingSupport.convertToJsonString(enrichedSchema);

        return new ParsedErrorResponse(
                statusCode,
                errorCode,
                errorCodeParts[0],
                errorCodeParts[1],
                errorCodeParts[2],
                errorName,
                message,
                description,
                schemaJson
        );
    }

    private String[] parseErrorCode(final String errorCode) {
        final String[] parts = new String[]{"", "", ""};

        if (errorCode == null || errorCode.isEmpty()) {
            return parts;
        }

        final String[] split = errorCode.split("-");
        if (split.length >= 1) parts[0] = split[0];
        if (split.length >= 2) parts[1] = split[1];
        if (split.length >= 3) parts[2] = split[2];

        return parts;
    }

    private String firstNonEmpty(final String... candidates) {
        if (candidates == null) {
            return "";
        }
        for (final String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return "";
    }
}
