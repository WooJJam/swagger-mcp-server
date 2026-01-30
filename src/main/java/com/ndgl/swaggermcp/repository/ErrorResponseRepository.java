package com.ndgl.swaggermcp.repository;

import com.ndgl.swaggermcp.entity.ErrorResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErrorResponseRepository extends JpaRepository<ErrorResponse, Long> {

    /**
     * API Endpoint ID로 모든 Error Response 조회
     */
    List<ErrorResponse> findByApiEndpointId(Long apiEndpointId);

    /**
     * API Endpoint ID와 Status Code로 Error Response 목록 조회
     */
    List<ErrorResponse> findByApiEndpointIdAndStatusCode(Long apiEndpointId, Integer statusCode);

    /**
     * Error Code로 Error Response 조회
     */
    Optional<ErrorResponse> findByErrorCode(String errorCode);

    /**
     * Error Code로 Error Response 목록 조회
     */
    List<ErrorResponse> findByErrorCodeIn(List<String> errorCodes);

    /**
     * Domain Code로 Error Response 목록 조회
     */
    List<ErrorResponse> findByDomainCode(String domainCode);

    /**
     * Domain Code와 Category Code로 Error Response 목록 조회
     */
    List<ErrorResponse> findByDomainCodeAndCategoryCode(String domainCode, String categoryCode);

    /**
     * API Endpoint ID 목록으로 Error Response 목록 조회
     */
    List<ErrorResponse> findByApiEndpointIdIn(List<Long> apiEndpointIds);

    /**
     * Error Code로 검색 (부분 일치)
     */
    List<ErrorResponse> findByErrorCodeContainingIgnoreCase(String errorCode);

    /**
     * Error Message로 검색 (부분 일치)
     */
    List<ErrorResponse> findByErrorMessageContainingIgnoreCase(String errorMessage);

    /**
     * 특정 Status Code 범위의 Error Response 조회 (4xx, 5xx 등)
     */
    @Query("""
        SELECT e FROM ErrorResponse e
        WHERE e.statusCode >= :minStatusCode AND e.statusCode < :maxStatusCode
        """)
    List<ErrorResponse> findByStatusCodeRange(@Param("minStatusCode") Integer minStatusCode,
                                               @Param("maxStatusCode") Integer maxStatusCode);

    /**
     * API Endpoint ID로 존재 여부 확인
     */
    boolean existsByApiEndpointId(Long apiEndpointId);
}
