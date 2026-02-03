package com.ndgl.swaggermcp.dto.parser;

/**
 * 파싱된 Request Body 정보
 * (POST, PUT, PATCH 등에서 사용)
 */
public record ParsedRequestBody(
        String dtoName,
        String schemaJson,
        String exampleJson,
        Boolean required,
        String contentType
) {
}
