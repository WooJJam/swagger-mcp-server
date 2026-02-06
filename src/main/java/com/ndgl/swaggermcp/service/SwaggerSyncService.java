package com.ndgl.swaggermcp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.swaggermcp.dto.parser.*;
import com.ndgl.swaggermcp.entity.*;
import com.ndgl.swaggermcp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Swagger 데이터 동기화 서비스
 * 파싱된 Swagger 데이터를 DB에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SwaggerSyncService {

    private final ApiEndpointRepository apiEndpointRepository;
    private final RequestSchemaRepository requestSchemaRepository;
    private final ParameterRepository parameterRepository;
    private final ResponseSchemaRepository responseSchemaRepository;
    private final ErrorResponseRepository errorResponseRepository;
    private final SwaggerMetadataRepository swaggerMetadataRepository;
    private final ObjectMapper objectMapper;

    /**
     * 전체 동기화 (Full Sync)
     * 기존 데이터를 모두 삭제하고 새로운 데이터 저장
     *
     * @param endpoints 파싱된 API 엔드포인트 리스트
     * @param swaggerUrl Swagger URL
     * @param swaggerVersion Swagger 버전
     */
    @Transactional
    public void syncAll(final List<ParsedApiEndpoint> endpoints, final String swaggerUrl, final String swaggerVersion) {
        log.info("전체 동기화 시작: {} 엔드포인트", endpoints.size());

        // 1. 기존 데이터 삭제
        deleteAll();

        // 2. API 엔드포인트 저장
        int savedCount = 0;
        for (final ParsedApiEndpoint parsedEndpoint : endpoints) {
            saveApiEndpoint(parsedEndpoint);
            savedCount++;
        }

        // 3. 메타데이터 업데이트
        updateMetadata(swaggerUrl, swaggerVersion, savedCount);

        log.info("전체 동기화 완료: {} 엔드포인트 저장됨", savedCount);
    }

    /**
     * 기존 데이터 전체 삭제
     */
    private void deleteAll() {
        log.info("기존 데이터 삭제 중...");
        errorResponseRepository.deleteAll();
        responseSchemaRepository.deleteAll();
        parameterRepository.deleteAll();
        requestSchemaRepository.deleteAll();
        apiEndpointRepository.deleteAll();
        log.info("기존 데이터 삭제 완료");
    }

    /**
     * API 엔드포인트 및 관련 데이터 저장
     */
    private void saveApiEndpoint(final ParsedApiEndpoint parsedEndpoint) {
        // 1. API 엔드포인트 저장
        final ApiEndpoint apiEndpoint = ApiEndpoint.builder()
                .path(parsedEndpoint.path())
                .method(parsedEndpoint.method())
                .operationId(parsedEndpoint.operationId())
                .summary(parsedEndpoint.summary())
                .description(parsedEndpoint.description())
                .tags(parsedEndpoint.tags())
                .build();

        final ApiEndpoint savedEndpoint = apiEndpointRepository.save(apiEndpoint);
        log.debug("API 엔드포인트 저장: {} {}", savedEndpoint.getMethod(), savedEndpoint.getPath());

        // 2. Request Body 저장 (있는 경우만)
        if (parsedEndpoint.requestBody() != null) {
            saveRequestBody(savedEndpoint.getId(), parsedEndpoint.requestBody());
        }

        // 3. Parameters 저장
        if (parsedEndpoint.parameters() != null && !parsedEndpoint.parameters().isEmpty()) {
            saveParameters(savedEndpoint.getId(), parsedEndpoint.parameters());
        }

        // 4. Response Schemas 저장
        if (parsedEndpoint.responseSchemas() != null && !parsedEndpoint.responseSchemas().isEmpty()) {
            saveResponseSchemas(savedEndpoint.getId(), parsedEndpoint.responseSchemas());
        }

        // 5. Error Responses 저장
        if (parsedEndpoint.errorResponses() != null && !parsedEndpoint.errorResponses().isEmpty()) {
            saveErrorResponses(savedEndpoint.getId(), parsedEndpoint.errorResponses());
        }
    }

    /**
     * Request Body 저장
     */
    private void saveRequestBody(final Long apiEndpointId, final ParsedRequestBody requestBody) {
        try {
            final Map<String, Object> schemaMap = convertJsonStringToMap(requestBody.schemaJson());
            final Map<String, Object> exampleMap = convertJsonStringToMap(requestBody.exampleJson());

            final RequestSchema requestSchema = RequestSchema.builder()
                    .apiEndpointId(apiEndpointId)
                    .dtoName(requestBody.dtoName())
                    .schemaJson(schemaMap)
                    .exampleJson(exampleMap)
                    .build();

            requestSchemaRepository.save(requestSchema);
            log.debug("Request Body 저장: {}", requestBody.dtoName());
        } catch (Exception e) {
            log.error("Request Body 저장 실패: {}", requestBody.dtoName(), e);
        }
    }

    /**
     * Parameters 저장
     */
    private void saveParameters(final Long apiEndpointId, final List<ParsedParameter> parameters) {
        for (final ParsedParameter param : parameters) {
            final Parameter parameter = Parameter.builder()
                    .apiEndpointId(apiEndpointId)
                    .name(param.name())
                    .in(param.in())
                    .required(param.required())
                    .type(param.type())
                    .format(param.format())
                    .description(param.description())
                    .build();

            parameterRepository.save(parameter);
        }
        log.debug("Parameters 저장: {} 개", parameters.size());
    }

    /**
     * Response Schemas 저장
     */
    private void saveResponseSchemas(final Long apiEndpointId, final List<ParsedResponseSchema> responseSchemas) {
        for (final ParsedResponseSchema response : responseSchemas) {
            try {
                final Map<String, Object> schemaMap = convertJsonStringToMap(response.schemaJson());
                final Map<String, Object> exampleMap = convertJsonStringToMap(response.exampleJson());

                final ResponseSchema responseSchema = ResponseSchema.builder()
                        .apiEndpointId(apiEndpointId)
                        .statusCode(response.statusCode())
                        .dtoName(response.dtoName())
                        .schemaJson(schemaMap)
                        .exampleJson(exampleMap)
                        .build();

                responseSchemaRepository.save(responseSchema);
                log.debug("Response Schema 저장: {} ({})", response.dtoName(), response.statusCode());
            } catch (Exception e) {
                log.error("Response Schema 저장 실패: {} ({})", response.dtoName(), response.statusCode(), e);
            }
        }
    }

    /**
     * Error Responses 저장
     */
    private void saveErrorResponses(final Long apiEndpointId, final List<ParsedErrorResponse> errorResponses) {
        for (final ParsedErrorResponse error : errorResponses) {
            try {
                final Map<String, Object> schemaMap = convertJsonStringToMap(error.schemaJson());

                final ErrorResponse errorResponse = ErrorResponse.builder()
                        .apiEndpointId(apiEndpointId)
                        .statusCode(error.statusCode())
                        .errorCode(error.errorCode())
                        .errorName(error.errorName())
                        .errorMessage(error.message())
                        .domainCode(error.domainCode())
                        .categoryCode(error.categoryCode())
                        .detailCode(error.detailCode())
                        .description(error.description())
                        .schemaJson(schemaMap)
                        .build();

                errorResponseRepository.save(errorResponse);
                log.debug("Error Response 저장: {} ({})", error.errorCode(), error.statusCode());
            } catch (Exception e) {
                log.error("Error Response 저장 실패: {} ({})", error.errorCode(), error.statusCode(), e);
            }
        }
    }

    /**
     * Swagger 메타데이터 업데이트
     */
    private void updateMetadata(final String swaggerUrl, final String swaggerVersion, final int apiCount) {
        // 기존 메타데이터가 있으면 삭제하고 새로 생성
        swaggerMetadataRepository.deleteAll();

        final SwaggerMetadata metadata = SwaggerMetadata.builder()
                .lastSyncedAt(LocalDateTime.now())
                .apiCount(apiCount)
                .backendUrl(swaggerUrl)
                .swaggerVersion(swaggerVersion)
                .build();

        swaggerMetadataRepository.save(metadata);
        log.info("메타데이터 업데이트: {} APIs, 버전 {}", apiCount, swaggerVersion);
    }

    /**
     * JSON 문자열을 Map으로 변환
     */
    private Map<String, Object> convertJsonStringToMap(final String jsonString) {
        if (jsonString == null || jsonString.isEmpty() || jsonString.equals("null")) {
            return null;
        }

        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("JSON 파싱 실패, null 반환: {}", jsonString, e);
            return null;
        }
    }
}
