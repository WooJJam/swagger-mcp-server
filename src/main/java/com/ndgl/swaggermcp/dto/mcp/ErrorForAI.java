package com.ndgl.swaggermcp.dto.mcp;

import java.util.Map;

/**
 * API Error 정보 (AI 친화적 포맷)
 */
public record ErrorForAI(
        Integer statusCode,
        String errorCode,
        String errorName,
        String message,
        String description,
        Map<String, FieldInfo> schema
) {
}
