package com.ndgl.swaggermcp.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.ndgl.swaggermcp.dto.parser.ParsedResponseSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseSchemaParser {

    private final JsonSchemaParsingSupport parsingSupport;

    public List<ParsedResponseSchema> parseResponseSchemas(final JsonNode swaggerJson, final JsonNode operation) {
        final List<ParsedResponseSchema> responses = new ArrayList<>();
        final JsonNode responsesNode = operation.path("responses");

        if (responsesNode.isMissingNode()) {
            return responses;
        }

        for (final Map.Entry<String, JsonNode> entry : responsesNode.properties()) {
            final String statusCodeStr = entry.getKey();
            final JsonNode response = entry.getValue();

            try {
                final int statusCode = Integer.parseInt(statusCodeStr);
                if (statusCode >= 200 && statusCode < 300) {
                    final ParsedResponseSchema parsedResponse = parseResponse(swaggerJson, statusCode, response);
					responses.add(parsedResponse);
				}
            } catch (NumberFormatException e) {
                log.debug("숫자가 아닌 상태 코드 건너뜀: {}", statusCodeStr);
            }
        }

        return responses;
    }

    private ParsedResponseSchema parseResponse(final JsonNode swaggerJson, final int statusCode, final JsonNode response) {
        final String description = response.path("description").asText("");
        final JsonNode content = response.path("content");

        if (content.isMissingNode()) {
            return new ParsedResponseSchema(
                    statusCode,
                    null,
                    null,
                    null,
                    description,
                    null
            );
        }

        final JsonNode jsonContent = parsingSupport.selectContentNode(content);
        if (jsonContent.isMissingNode()) {
            return new ParsedResponseSchema(
                    statusCode,
                    null,
                    null,
                    null,
                    description,
                    null
            );
        }

        final JsonNode schema = jsonContent.path("schema");
        final String schemaRef = schema.path("$ref").asText("");
        final String dtoName = parsingSupport.extractDtoNameFromRef(schemaRef);

        // $ref를 resolve하여 실제 schema 가져오기
        final JsonNode resolvedSchema = schemaRef.isEmpty()
                ? schema
                : parsingSupport.resolveSchemaRef(swaggerJson, schemaRef);

        final JsonSchemaParsingSupport.ExampleData exampleData = parsingSupport.extractExample(jsonContent);
        final String exampleJson = parsingSupport.convertToJsonString(exampleData.value());
        final String schemaJson = parsingSupport.convertToJsonString(resolvedSchema != null ? resolvedSchema : schema);

        return new ParsedResponseSchema(
                statusCode,
                dtoName,
                schemaJson,
                exampleJson,
                description,
                "application/json"
        );
    }
}
