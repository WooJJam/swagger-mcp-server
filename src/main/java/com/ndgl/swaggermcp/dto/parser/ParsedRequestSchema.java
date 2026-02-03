package com.ndgl.swaggermcp.dto.parser;

import java.util.List;

/**
 * 파싱된 Request Schema 정보
 * (RequestBody + Parameters)
 */
public record ParsedRequestSchema(
        String dtoName,
        String schemaJson,
        String exampleJson,
        Boolean required,
        String contentType,
        List<ParsedParameter> parameters
) {
}
