package com.ndgl.swaggermcp.dto.facade;

import com.ndgl.swaggermcp.dto.parser.ParsedApiEndpoint;
import com.ndgl.swaggermcp.dto.parser.ParsedTag;

import java.util.List;

/**
 * Swagger 파싱 결과 DTO
 */
public record SwaggerParseResult(
        List<ParsedApiEndpoint> endpoints,
        List<ParsedTag> tags,
        int totalEndpoints,
        int totalTags
) {
}
