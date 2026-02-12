package com.ndgl.swaggermcp.ai.dto;

import java.util.List;
import java.util.Map;

/**
 * API Error 정보 (AI 친화적 포맷)
 */
public record ErrorForAI(
        Integer statusCode,
        String code,
        String message,
        String description,
        Map<String, FieldInfo> schema,
        List<Map<String, Object>> errors
) {
}
