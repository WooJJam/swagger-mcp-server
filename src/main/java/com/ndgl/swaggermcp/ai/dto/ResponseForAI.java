package com.ndgl.swaggermcp.ai.dto;

import java.util.Map;

/**
 * API Response 정보 (AI 친화적 포맷)
 */
public record ResponseForAI(
        Integer statusCode,
        String description,
        Map<String, FieldInfo> body
) {
}
