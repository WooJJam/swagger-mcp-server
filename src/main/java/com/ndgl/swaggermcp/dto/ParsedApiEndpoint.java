package com.ndgl.swaggermcp.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 파싱된 API 엔드포인트 정보
 */
@Getter
@Builder
public class ParsedApiEndpoint {

    /**
     * API 경로 (예: /api/v1/travels/{id})
     */
    private String path;

    /**
     * HTTP 메서드 (예: GET, POST, PUT, DELETE)
     */
    private String method;

    /**
     * Operation ID (예: getTravelById)
     */
    private String operationId;

    /**
     * API 요약 설명 (예: 여행 조회)
     */
    private String summary;

    /**
     * API 상세 설명
     */
    private String description;

    /**
     * Tags (JSON 문자열)
     */
    private String tagsJson;

    /**
     * Tags 리스트 (파싱 시 사용)
     */
    private List<String> tags;
}
