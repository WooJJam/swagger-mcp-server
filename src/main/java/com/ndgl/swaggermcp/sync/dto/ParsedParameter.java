package com.ndgl.swaggermcp.sync.dto;

/**
 * 파싱된 API 파라미터 정보
 * (Path, Query, Header, Cookie 파라미터)
 */
public record ParsedParameter(
        String name,
        String in,
        Boolean required,
        String type,
        String format,
        String description
) {
}
