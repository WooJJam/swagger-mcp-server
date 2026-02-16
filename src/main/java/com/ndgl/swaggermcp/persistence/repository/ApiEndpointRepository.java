package com.ndgl.swaggermcp.persistence.repository;

import com.ndgl.swaggermcp.persistence.entity.ApiEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiEndpointRepository extends JpaRepository<ApiEndpoint, Long> {

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
}
