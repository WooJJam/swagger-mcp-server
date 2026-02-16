package com.ndgl.swaggermcp.persistence.repository;

import com.ndgl.swaggermcp.persistence.entity.SwaggerMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwaggerMetadataRepository extends JpaRepository<SwaggerMetadata, Long> {
}
