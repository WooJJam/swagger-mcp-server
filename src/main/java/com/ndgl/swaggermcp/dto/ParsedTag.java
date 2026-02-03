package com.ndgl.swaggermcp.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 파싱된 Tag 정보
 */
@Getter
@Builder
public class ParsedTag {

    /**
     * Tag 이름 (예: Travel, User)
     */
    private String name;

    /**
     * Tag 설명 (예: 여행 관련 API)
     */
    private String description;
}
