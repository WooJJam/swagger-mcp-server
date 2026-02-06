package com.ndgl.swaggermcp.service;

import com.ndgl.swaggermcp.dto.mcp.*;
import com.ndgl.swaggermcp.entity.*;
import com.ndgl.swaggermcp.formatter.SchemaFormatter;
import com.ndgl.swaggermcp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * API 검색 서비스 (MCP Tool용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiSearchService {

    private final ApiEndpointRepository apiEndpointRepository;
    private final RequestSchemaRepository requestSchemaRepository;
    private final ParameterRepository parameterRepository;
    private final ResponseSchemaRepository responseSchemaRepository;
    private final ErrorResponseRepository errorResponseRepository;
    private final SchemaFormatter schemaFormatter;

    /**
     * 키워드로 API 검색
     *
     * @param keyword 검색 키워드
     * @return API 요약 리스트
     */
    public List<ApiSummary> searchApiByKeyword(final String keyword) {
        log.info("API 검색 시작: {}", keyword);

        final List<ApiEndpoint> endpoints = apiEndpointRepository.findAll();

        // 키워드가 path, summary, description, tags 중 하나라도 포함되면 매칭
        final List<ApiSummary> results = endpoints.stream()
            .filter(endpoint -> matchesKeyword(endpoint, keyword))
            .map(endpoint -> new ApiSummary(
                endpoint.getId(),
                endpoint.getPath(),
                endpoint.getMethod(),
                endpoint.getSummary(),
                endpoint.getTags()
            ))
            .toList();

        log.info("API 검색 완료: {} 건 발견", results.size());
        return results;
    }

    /**
     * API 상세 정보 조회
     *
     * @param apiId API ID
     * @return API 상세 정보 (AI 친화적 포맷)
     */
    public ApiDetailForAI getApiDetail(final Long apiId) {
        log.info("API 상세 조회: {}", apiId);

        final ApiEndpoint endpoint = apiEndpointRepository.findById(apiId)
            .orElseThrow(() -> new IllegalArgumentException("API를 찾을 수 없습니다: " + apiId));

        // 1. Request 정보 조회
        final RequestForAI request = getRequestFormat(apiId);

        // 2. Response 정보 조회
        final Map<Integer, ResponseForAI> responses = getResponseFormat(apiId);

        // 3. Error 정보 조회
        final Map<Integer, ErrorForAI> errors = getErrorFormats(apiId);

        return new ApiDetailForAI(
            endpoint.getId(),
            endpoint.getPath(),
            endpoint.getMethod(),
            endpoint.getOperationId(),
            endpoint.getSummary(),
            endpoint.getDescription(),
            endpoint.getTags(),
            request,
            responses,
            errors
        );
    }

    /**
     * Request 포맷 조회
     *
     * @param apiId API ID
     * @return Request 포맷 (AI 친화적)
     */
    public RequestForAI getRequestFormat(final Long apiId) {
        // 1. Request Body 조회
        final List<RequestSchema> requestSchemas = requestSchemaRepository.findAll().stream()
            .filter(rs -> rs.getApiEndpointId().equals(apiId))
            .toList();

        Map<String, FieldInfo> body = Collections.emptyMap();
        if (!requestSchemas.isEmpty()) {
            final RequestSchema requestSchema = requestSchemas.get(0);
            body = schemaFormatter.formatSchema(requestSchema.getSchemaJson(), requestSchema.getExampleJson());
        }

        // 2. Parameters 조회
        final List<Parameter> parameters = parameterRepository.findAll().stream()
            .filter(p -> p.getApiEndpointId().equals(apiId))
            .toList();

        final List<ParameterInfo> parameterInfos = schemaFormatter.formatParameters(parameters);

        return new RequestForAI(body, parameterInfos);
    }

    /**
     * Response 포맷 조회
     *
     * @param apiId API ID
     * @return 상태 코드별 Response 포맷
     */
    public Map<Integer, ResponseForAI> getResponseFormat(final Long apiId) {
        final List<ResponseSchema> responseSchemas = responseSchemaRepository.findAll().stream()
            .filter(rs -> rs.getApiEndpointId().equals(apiId))
            .toList();

        return responseSchemas.stream()
            .collect(Collectors.toMap(
                ResponseSchema::getStatusCode,
                rs -> new ResponseForAI(
                    rs.getStatusCode(),
                    "Success",
                    schemaFormatter.formatSchema(rs.getSchemaJson(), rs.getExampleJson())
                )
            ));
    }

    /**
     * Error 포맷 조회
     *
     * @param apiId API ID
     * @return 상태 코드별 Error 포맷
     */
    public Map<Integer, ErrorForAI> getErrorFormats(final Long apiId) {
        final List<ErrorResponse> errorResponses = errorResponseRepository.findAll().stream()
            .filter(er -> er.getApiEndpointId().equals(apiId))
            .toList();

        return errorResponses.stream()
            .collect(Collectors.toMap(
                ErrorResponse::getStatusCode,
                er -> new ErrorForAI(
                    er.getStatusCode(),
                    er.getErrorCode(),
                    er.getErrorName(),
                    er.getErrorMessage(),
                    er.getDescription(),
                    schemaFormatter.formatSchema(er.getSchemaJson(), null)
                ),
                (existing, replacement) -> existing // 중복 키 처리
            ));
    }

    /**
     * 키워드 매칭 여부 확인
     */
    private boolean matchesKeyword(final ApiEndpoint endpoint, final String keyword) {
        final String lowerKeyword = keyword.toLowerCase();

        // Path 매칭
        if (endpoint.getPath() != null && endpoint.getPath().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // Summary 매칭
        if (endpoint.getSummary() != null && endpoint.getSummary().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // Description 매칭
        if (endpoint.getDescription() != null && endpoint.getDescription().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // Tags 매칭
        if (endpoint.getTags() != null) {
            for (final String tag : endpoint.getTags()) {
                if (tag.toLowerCase().contains(lowerKeyword)) {
                    return true;
                }
            }
        }

        return false;
    }
}
