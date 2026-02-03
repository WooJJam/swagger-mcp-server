package com.ndgl.swaggermcp.dto.parser;

import java.util.List;

/**
 * Operation 기본 정보 및 Tag 파싱 결과
 */
public record ParsedEndpointBasicInfo(
        /**
         * API 경로 (예: /api/v1/travels/{id})
         */
        String path,

        /**
         * HTTP 메서드 (예: GET, POST, PUT, DELETE)
         */
        String method,

        /**
         * Operation ID (예: getTravelById)
         */
        String operationId,

        /**
         * API 요약 설명 (예: 여행 조회)
         */
        String summary,

        /**
         * API 상세 설명
         */
        String description,

        /**
         * Tags (JSON 문자열)
         */
        String tagsJson,

        /**
         * Tags 리스트
         */
        List<String> tags
) {
}
