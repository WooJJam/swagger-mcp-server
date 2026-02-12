package com.ndgl.swaggermcp.sync.dto;

import java.util.List;
import java.util.Map;

/**
 * 파싱된 Error Response 정보
 */
public record ParsedErrorResponse(
        Integer statusCode,
        String code,
        String domainCode,
        String categoryCode,
        String detailCode,
        String message,
        String description,
        String schemaJson,
        List<Map<String, Object>> errors
) {
}
