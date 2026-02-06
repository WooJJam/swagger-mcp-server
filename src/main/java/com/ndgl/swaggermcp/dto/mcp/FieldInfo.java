package com.ndgl.swaggermcp.dto.mcp;

/**
 * API 필드 정보 (AI 친화적 포맷)
 */
public record FieldInfo(
        String type,
        String format,
        Boolean required,
        String description,
        Object example
) {
}
