package com.ndgl.swaggermcp.sync.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ndgl.swaggermcp.sync.dto.ParsedApiEndpoint;
import com.ndgl.swaggermcp.sync.dto.ParsedEndpointBasicInfo;
import com.ndgl.swaggermcp.sync.dto.ParsedErrorResponse;
import com.ndgl.swaggermcp.sync.dto.ParsedParameter;
import com.ndgl.swaggermcp.sync.dto.ParsedRequestBody;
import com.ndgl.swaggermcp.sync.dto.ParsedResponseSchema;
import com.ndgl.swaggermcp.sync.dto.ParsedTag;
import com.ndgl.swaggermcp.sync.application.parser.EndpointBasicInfoParser;
import com.ndgl.swaggermcp.sync.application.parser.ErrorResponseParser;
import com.ndgl.swaggermcp.sync.application.parser.RequestSchemaParser;
import com.ndgl.swaggermcp.sync.application.parser.ResponseSchemaParser;
import com.ndgl.swaggermcp.sync.application.parser.TagParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API 엔드포인트 및 Tag 파싱 전담 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SwaggerParserService {

    private final EndpointBasicInfoParser basicInfoParser;
    private final RequestSchemaParser requestSchemaParser;
    private final ResponseSchemaParser responseSchemaParser;
    private final ErrorResponseParser errorResponseParser;
    private final TagParser tagParser;

    public List<ParsedApiEndpoint> parseApiEndpoints(final JsonNode swaggerJson) {
        log.info("API 엔드포인트 파싱 시작");
        final List<ParsedApiEndpoint> endpoints = new ArrayList<>();

        final JsonNode paths = swaggerJson.path("paths");
        if (paths.isMissingNode()) {
            log.warn("Swagger JSON에 'paths' 필드가 없습니다");
            return endpoints;
        }

        for (final Map.Entry<String, JsonNode> pathEntry : paths.properties()) {
            final String path = pathEntry.getKey(); // api endpoint
            final JsonNode methods = pathEntry.getValue();

            for (final Map.Entry<String, JsonNode> methodEntry : methods.properties()) {
                final String method = methodEntry.getKey().toUpperCase();
                final JsonNode operation = methodEntry.getValue();

                final ParsedApiEndpoint endpoint = parseOperation(swaggerJson, path, method, operation);
                endpoints.add(endpoint);

                log.debug("엔드포인트 파싱 완료: {} {}", method, path);
            }
        }

        log.info("API 엔드포인트 파싱 완료: {}개", endpoints.size());
        return endpoints;
    }

    public List<ParsedTag> parseTags(final JsonNode swaggerJson) {
        return tagParser.parseTags(swaggerJson);
    }

    private ParsedApiEndpoint parseOperation(final JsonNode swaggerJson, final String path, final String method, final JsonNode operation) {
        // 1. 기본 정보 파싱
        final ParsedEndpointBasicInfo basicInfo = basicInfoParser.parseBasicInfo(path, method, operation);

        // 2. Request 파싱 (Body + Parameters)
        final ParsedRequestBody requestBody = requestSchemaParser.parseRequestBody(swaggerJson, operation);
        final List<ParsedParameter> parameters = requestSchemaParser.parseParameters(operation);

        // 3. Response 파싱
        final List<ParsedResponseSchema> responseSchemas = responseSchemaParser.parseResponseSchemas(swaggerJson, operation);

        // 4. Error 파싱
        final List<ParsedErrorResponse> errorResponses = errorResponseParser.parseErrorResponses(swaggerJson, operation);

        return new ParsedApiEndpoint(
                basicInfo.path(),
                basicInfo.method(),
                basicInfo.operationId(),
                basicInfo.summary(),
                basicInfo.description(),
                basicInfo.tagsJson(),
                basicInfo.tags(),
                requestBody,
                parameters,
                responseSchemas,
                errorResponses
        );
    }
}
