package com.ndgl.swaggermcp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "swagger_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SwaggerMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Column(name = "api_count", nullable = false)
    private Integer apiCount;

    @Column(name = "backend_url", length = 255)
    private String backendUrl;

    @Column(name = "swagger_version", length = 50)
    private String swaggerVersion;

    @Builder
    public SwaggerMetadata(final Long id, final LocalDateTime lastSyncedAt, final Integer apiCount,
                           final String backendUrl, final String swaggerVersion) {
        this.id = id;
        this.lastSyncedAt = lastSyncedAt;
        this.apiCount = apiCount;
        this.backendUrl = backendUrl;
        this.swaggerVersion = swaggerVersion;
    }
}
