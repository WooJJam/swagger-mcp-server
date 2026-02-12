package com.ndgl.swaggermcp.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "error_responses", indexes = {
    @Index(name = "idx_code", columnList = "code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_endpoint_id", nullable = false)
    private Long apiEndpointId;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "domain_code", length = 20)
    private String domainCode;

    @Column(name = "category_code", length = 20)
    private String categoryCode;

    @Column(name = "detail_code", length = 20)
    private String detailCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_json", columnDefinition = "JSON")
    private Map<String, Object> schemaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "errors", columnDefinition = "JSON")
    private List<Map<String, Object>> errors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_endpoint_id", insertable = false, updatable = false)
    private ApiEndpoint apiEndpoint;

    @Builder
    public ErrorResponse(final Long id, final Long apiEndpointId, final Integer statusCode,
                         final String code, final String message,
                         final String domainCode, final String categoryCode, final String detailCode,
                         final String description, final Map<String, Object> schemaJson,
                         final List<Map<String, Object>> errors) {
        this.id = id;
        this.apiEndpointId = apiEndpointId;
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
        this.domainCode = domainCode;
        this.categoryCode = categoryCode;
        this.detailCode = detailCode;
        this.description = description;
        this.schemaJson = schemaJson;
        this.errors = errors;
    }
}
