package com.ndgl.swaggermcp.sync.application.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.ndgl.swaggermcp.sync.dto.ParsedParameter;
import com.ndgl.swaggermcp.sync.dto.ParsedRequestBody;
import com.ndgl.swaggermcp.sync.support.JsonSchemaParsingSupport;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestSchemaParser {

    private final JsonSchemaParsingSupport parsingSupport;

    /**
     * RequestBody 파싱 (POST, PUT, PATCH 등)
     */
    public ParsedRequestBody parseRequestBody(final JsonNode swaggerJson, final JsonNode operation) {
        final JsonNode requestBody = operation.path("requestBody");
        if (requestBody.isMissingNode()) {
            return null;
        }

        final boolean required = requestBody.path("required").asBoolean(false);
        final JsonNode content = requestBody.path("content");

        final JsonNode jsonContent = parsingSupport.selectContentNode(content);
        if (jsonContent.isMissingNode()) {
            return null;
        }

        final JsonNode schema = jsonContent.path("schema");
        final String schemaRef = schema.path("$ref").asText("");
        final String dtoName = parsingSupport.extractDtoNameFromRef(schemaRef);

        // $ref를 resolve하여 실제 schema 가져오기
        final JsonNode resolvedSchema = schemaRef.isEmpty()
                ? schema
                : parsingSupport.resolveSchemaRef(swaggerJson, schemaRef);

        // required 정보를 각 필드에 포함시킨 enriched schema 생성
        final JsonNode enrichedSchema = parsingSupport.enrichSchemaWithRequired(
                resolvedSchema != null ? resolvedSchema : schema
        );

        final JsonSchemaParsingSupport.ExampleData exampleData = parsingSupport.extractExample(jsonContent, resolvedSchema);
        final String exampleJson = parsingSupport.convertToJsonString(exampleData.value());
        final String schemaJson = parsingSupport.convertToJsonString(enrichedSchema);

        return new ParsedRequestBody(
                dtoName,
                schemaJson,
                exampleJson,
                required,
                "application/json"
        );
    }

    /**
     * Parameters 파싱 (Path, Query, Header, Cookie)
     */
    public List<ParsedParameter> parseParameters(final JsonNode operation) {
        final List<ParsedParameter> parameters = new ArrayList<>();
        final JsonNode parametersNode = operation.path("parameters");

        if (parametersNode.isMissingNode() || !parametersNode.isArray()) {
            return parameters;
        }

        parametersNode.forEach(paramNode -> {
            final String name = paramNode.path("name").asText("");
            final String in = paramNode.path("in").asText("");
            final Boolean required = paramNode.path("required").asBoolean(false);
            final String description = paramNode.path("description").asText("");

            // schema에서 type과 format 추출
            final JsonNode schema = paramNode.path("schema");
            final String type = schema.path("type").asText("");
            final String format = schema.path("format").asText(null);

            if (!name.isEmpty() && !in.isEmpty()) {
                final ParsedParameter parameter = new ParsedParameter(
                        name,
                        in,
                        required,
                        type,
                        format,
                        description
                );
                parameters.add(parameter);
            }
        });

        return parameters;
    }
}
