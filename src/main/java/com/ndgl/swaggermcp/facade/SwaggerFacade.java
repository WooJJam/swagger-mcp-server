package com.ndgl.swaggermcp.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.ndgl.swaggermcp.dto.facade.SwaggerSyncResult;
import com.ndgl.swaggermcp.dto.parser.ParsedApiEndpoint;
import com.ndgl.swaggermcp.service.SwaggerExtractorService;
import com.ndgl.swaggermcp.service.SwaggerFetchService;
import com.ndgl.swaggermcp.service.SwaggerParserService;
import com.ndgl.swaggermcp.service.SwaggerSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Swagger 통합 Facade
 * Fetch, Parse, Sync 서비스를 조합하여 최상위 비즈니스 로직 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerFacade {

    private final SwaggerFetchService swaggerFetchService;
    private final SwaggerParserService swaggerParserService;
    private final SwaggerExtractorService swaggerExtractorService;
    private final SwaggerSyncService swaggerSyncService;

    /**
     * Swagger JSON을 가져와서 파싱 후 DB에 저장
     *
     * @param swaggerUrl Swagger JSON URL
     * @return 동기화 결과
     */
    public SwaggerSyncResult syncSwagger(final String swaggerUrl) {
        log.info("Swagger 동기화 시작: {}", swaggerUrl);

        try {
            // 1. Swagger JSON 다운로드
            final JsonNode swaggerJson = swaggerFetchService.fetchSwaggerJson(swaggerUrl);

            // 2. 버전 추출
            final String swaggerVersion = swaggerExtractorService.extractVersion(swaggerJson);

            // 3. 파싱
            final List<ParsedApiEndpoint> endpoints = swaggerParserService.parseApiEndpoints(swaggerJson);

            // 4. DB 저장
            swaggerSyncService.syncAll(endpoints, swaggerUrl, swaggerVersion);

            log.info("Swagger 동기화 완료: {} 엔드포인트", endpoints.size());

            return new SwaggerSyncResult(
                true,
                "동기화 성공",
                endpoints.size(),
                swaggerVersion
            );
        } catch (Exception e) {
            log.error("Swagger 동기화 실패: {}", swaggerUrl, e);
            return new SwaggerSyncResult(
                false,
                "동기화 실패: " + e.getMessage(),
                0,
                null
            );
        }
    }
}
