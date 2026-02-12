package com.ndgl.swaggermcp.sync.dto;

import java.util.List;

/**
 * 파싱된 API 엔드포인트 정보
 */
public record ParsedApiEndpoint(
        String path,
        String method,
        String operationId,
        String summary,
        String description,
        String tagsJson,
        List<String> tags,
        ParsedRequestBody requestBody,
        List<ParsedParameter> parameters,
        List<ParsedResponseSchema> responseSchemas,
        List<ParsedErrorResponse> errorResponses
) {
}
