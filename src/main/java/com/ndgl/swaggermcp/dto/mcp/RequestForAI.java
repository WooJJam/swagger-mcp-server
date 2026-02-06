package com.ndgl.swaggermcp.dto.mcp;

import java.util.List;
import java.util.Map;

/**
 * API Request 정보 (AI 친화적 포맷)
 */
public record RequestForAI(
        Map<String, FieldInfo> body,
        List<ParameterInfo> parameters
) {
}
