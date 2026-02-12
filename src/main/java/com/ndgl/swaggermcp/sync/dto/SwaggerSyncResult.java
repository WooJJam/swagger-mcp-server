package com.ndgl.swaggermcp.sync.dto;

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
