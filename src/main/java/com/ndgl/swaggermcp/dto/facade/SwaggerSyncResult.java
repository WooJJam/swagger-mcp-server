package com.ndgl.swaggermcp.dto.facade;

/**
 * Swagger 동기화 결과
 */
public record SwaggerSyncResult(
        Boolean success,
        String message,
        Integer totalEndpoints,
        String swaggerVersion
) {
}
