package com.ndgl.swaggermcp.repository;

import com.ndgl.swaggermcp.entity.ApiEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiEndpointRepository extends JpaRepository<ApiEndpoint, Long> {

    /**
     * operationId로 API 조회
     */
    Optional<ApiEndpoint> findByOperationId(String operationId);

    /**
     * HTTP method로 API 목록 조회
     */
    List<ApiEndpoint> findByMethod(String method);

    /**
     * 키워드로 API 검색 (path, summary, description, tags 검색)
     */
    @Query("""
        SELECT DISTINCT a FROM ApiEndpoint a
        WHERE LOWER(a.path) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.operationId) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    List<ApiEndpoint> searchByKeyword(@Param("keyword") String keyword);

    /**
     * path로 API 목록 조회 (부분 일치)
     */
    List<ApiEndpoint> findByPathContainingIgnoreCase(String path);

    /**
     * method와 path로 API 조회
     */
    Optional<ApiEndpoint> findByMethodAndPath(String method, String path);

    /**
     * 모든 API를 생성일시 역순으로 조회
     */
    List<ApiEndpoint> findAllByOrderByCreatedAtDesc();
}
