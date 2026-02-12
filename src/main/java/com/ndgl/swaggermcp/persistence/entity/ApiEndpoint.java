package com.ndgl.swaggermcp.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "api_endpoints")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiEndpoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String path;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(unique = true, length = 255)
    private String operationId;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON", nullable = true)
    private List<String> tags;

    @Builder
    public ApiEndpoint(final Long id, final String path, final String method, final String operationId,
                       final String summary, final String description, final List<String> tags) {
        this.id = id;
        this.path = path;
        this.method = method;
        this.operationId = operationId;
        this.summary = summary;
        this.description = description;
        this.tags = tags;
    }
}
