package com.ndgl.swaggermcp.mcp;

import com.ndgl.swaggermcp.dto.mcp.*;
import com.ndgl.swaggermcp.service.ApiSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Swagger MCP Tools
 * Claude가 사용할 수 있는 API 검색 및 조회 도구
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerMcpTools {

    private final ApiSearchService apiSearchService;

    /**
     * 키워드로 API 검색
     *
     * @param keyword 검색 키워드 (path, summary, description, tags에서 검색)
     * @return API 요약 리스트
     */
    @McpTool(description = "Search API endpoints by keyword. Searches in path, summary, description, and tags.")
    public List<ApiSummary> searchApiByKeyword(
        @McpToolParam(description = "Search keyword", required = true) final String keyword
    ) {
        log.info("[MCP Tool] searchApiByKeyword 호출: {}", keyword);
        return apiSearchService.searchApiByKeyword(keyword);
    }

    /**
     * API 상세 정보 조회
     *
     * @param apiId API ID
     * @return API 상세 정보 (Request, Response, Error 포함)
     */
    @McpTool(description = "Get detailed API information including request body, parameters, responses, and errors. Returns AI-friendly format.")
    public ApiDetailForAI getApiDetail(
        @McpToolParam(description = "API endpoint ID", required = true) final Long apiId
    ) {
        log.info("[MCP Tool] getApiDetail 호출: {}", apiId);
        return apiSearchService.getApiDetail(apiId);
    }

    /**
     * API Request 포맷 조회
     *
     * @param apiId API ID
     * @return Request 포맷 (Body + Parameters)
     */
    @McpTool(description = "Get request format for API endpoint including body schema and parameters.")
    public RequestForAI getRequestFormat(
        @McpToolParam(description = "API endpoint ID", required = true) final Long apiId
    ) {
        log.info("[MCP Tool] getRequestFormat 호출: {}", apiId);
        return apiSearchService.getRequestFormat(apiId);
    }

    /**
     * API Response 포맷 조회
     *
     * @param apiId API ID
     * @return 상태 코드별 Response 포맷
     */
    @McpTool(description = "Get success response formats for API endpoint by status code.")
    public Map<Integer, ResponseForAI> getResponseFormat(
        @McpToolParam(description = "API endpoint ID", required = true) final Long apiId
    ) {
        log.info("[MCP Tool] getResponseFormat 호출: {}", apiId);
        return apiSearchService.getResponseFormat(apiId);
    }

    /**
     * API Error 포맷 조회
     *
     * @param apiId API ID
     * @return 상태 코드별 Error 포맷
     */
    @McpTool(description = "Get error response formats for API endpoint by status code. Includes error codes and messages.")
    public Map<Integer, ErrorForAI> getErrorFormats(
        @McpToolParam(description = "API endpoint ID", required = true) final Long apiId
    ) {
        log.info("[MCP Tool] getErrorFormats 호출: {}", apiId);
        return apiSearchService.getErrorFormats(apiId);
    }
}
