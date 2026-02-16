package com.ndgl.swaggermcp.persistence.repository;

import com.ndgl.swaggermcp.persistence.entity.ErrorResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * Domain Code로 Error Response 목록 조회
     */
    List<ErrorResponse> findByDomainCode(String domainCode);

    /**
     * API Endpoint ID 목록으로 Error Response 목록 조회
     */
    List<ErrorResponse> findByApiEndpointIdIn(List<Long> apiEndpointIds);

    /**
     * API Endpoint ID로 존재 여부 확인
     */
    boolean existsByApiEndpointId(Long apiEndpointId);
}
