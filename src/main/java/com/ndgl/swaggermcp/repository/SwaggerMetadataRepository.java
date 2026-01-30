package com.ndgl.swaggermcp.repository;

import com.ndgl.swaggermcp.entity.SwaggerMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SwaggerMetadataRepository extends JpaRepository<SwaggerMetadata, Long> {

    /**
     * 가장 최근 동기화 메타데이터 조회
     */
    Optional<SwaggerMetadata> findFirstByOrderByLastSyncedAtDesc();

    /**
     * Backend URL로 메타데이터 조회
     */
    Optional<SwaggerMetadata> findByBackendUrl(String backendUrl);
}
