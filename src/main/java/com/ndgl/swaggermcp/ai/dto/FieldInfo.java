package com.ndgl.swaggermcp.ai.dto;

import java.util.Map;

/**
 * API 필드 정보 (AI 친화적 포맷)
 * 중첩 객체/배열 구조를 재귀적으로 표현
 */
public record FieldInfo(
        String type,
        String format,
        Boolean required,
        String description,
        Object example,
        Map<String, FieldInfo> properties,
        FieldInfo items
) {
}
