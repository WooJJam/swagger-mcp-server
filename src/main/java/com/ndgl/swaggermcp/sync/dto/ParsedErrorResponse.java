package com.ndgl.swaggermcp.sync.dto;

/**
 * 파싱된 Error Response 정보
 */
public record ParsedErrorResponse(
        Integer statusCode,
        String errorCode,
        String domainCode,
        String categoryCode,
        String detailCode,
        String errorName,
        String message,
        String description,
        String schemaJson
) {
}
