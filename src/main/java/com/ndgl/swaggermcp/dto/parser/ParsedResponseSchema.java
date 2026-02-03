package com.ndgl.swaggermcp.dto.parser;

/**
 * 파싱된 Response Schema 정보
 */
public record ParsedResponseSchema(
        Integer statusCode,
        String dtoName,
        String schemaJson,
        String exampleJson,
        String description,
        String contentType
) {
}
