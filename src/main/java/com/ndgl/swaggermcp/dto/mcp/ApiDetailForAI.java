package com.ndgl.swaggermcp.dto.mcp;

import java.util.List;
import java.util.Map;

/**
 * API 상세 정보 (MCP Tool 응답용 메인 DTO)
 * Claude가 이해하기 쉬운 AI 친화적 포맷
 */
public record ApiDetailForAI(
        Long id,
        String path,
        String method,
        String operationId,
        String summary,
        String description,
        List<String> tags,
        RequestForAI request,
        Map<Integer, ResponseForAI> responses,
        Map<Integer, ErrorForAI> errors
) {
}
