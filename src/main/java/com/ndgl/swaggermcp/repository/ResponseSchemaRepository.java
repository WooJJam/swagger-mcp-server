package com.ndgl.swaggermcp.repository;

import com.ndgl.swaggermcp.entity.ResponseSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponseSchemaRepository extends JpaRepository<ResponseSchema, Long> {

    /**
     * API Endpoint ID로 모든 Response Schema 조회
     */
    List<ResponseSchema> findByApiEndpointId(Long apiEndpointId);

    /**
     * API Endpoint ID와 Status Code로 Response Schema 조회
     */
    Optional<ResponseSchema> findByApiEndpointIdAndStatusCode(Long apiEndpointId, Integer statusCode);

    /**
     * API Endpoint ID 목록으로 Response Schema 목록 조회
     */
    List<ResponseSchema> findByApiEndpointIdIn(List<Long> apiEndpointIds);

    /**
     * Success Response만 조회 (200, 201)
     */
    List<ResponseSchema> findByApiEndpointIdAndStatusCodeIn(Long apiEndpointId, List<Integer> statusCodes);

    /**
     * DTO 이름으로 Response Schema 검색
     */
    List<ResponseSchema> findByDtoNameContainingIgnoreCase(String dtoName);

    /**
     * API Endpoint ID로 존재 여부 확인
     */
    boolean existsByApiEndpointId(Long apiEndpointId);
}
