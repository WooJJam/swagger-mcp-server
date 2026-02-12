package com.ndgl.swaggermcp.persistence.repository;

import com.ndgl.swaggermcp.persistence.entity.RequestSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestSchemaRepository extends JpaRepository<RequestSchema, Long> {

    /**
     * API Endpoint ID로 Request Schema 조회
     */
    Optional<RequestSchema> findByApiEndpointId(Long apiEndpointId);

    /**
     * API Endpoint ID 목록으로 Request Schema 목록 조회
     */
    List<RequestSchema> findByApiEndpointIdIn(List<Long> apiEndpointIds);

    /**
     * DTO 이름으로 Request Schema 검색
     */
    List<RequestSchema> findByDtoNameContainingIgnoreCase(String dtoName);

    /**
     * API Endpoint ID로 존재 여부 확인
     */
    boolean existsByApiEndpointId(Long apiEndpointId);
}
