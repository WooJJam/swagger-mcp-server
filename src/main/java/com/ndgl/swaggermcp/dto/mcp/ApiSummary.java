package com.ndgl.swaggermcp.dto.mcp;

import java.util.List;

/**
 * API 요약 정보 (검색 결과용)
 */
public record ApiSummary(
        Long id,
        String path,
        String method,
        String summary,
        List<String> tags
) {
}
