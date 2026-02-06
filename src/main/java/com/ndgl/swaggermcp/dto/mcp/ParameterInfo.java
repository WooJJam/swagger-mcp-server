package com.ndgl.swaggermcp.dto.mcp;

/**
 * API 파라미터 정보 (AI 친화적 포맷)
 */
public record ParameterInfo(
        String name,
        String in,
        String type,
        String format,
        Boolean required,
        String description
) {
}
