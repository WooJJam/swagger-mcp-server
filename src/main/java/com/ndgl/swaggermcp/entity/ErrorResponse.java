package com.ndgl.swaggermcp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "error_responses", indexes = {
    @Index(name = "idx_error_code", columnList = "error_code")
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

    @Column(name = "error_code", nullable = false, length = 50)
    private String errorCode;

    @Column(name = "error_name", length = 100)
    private String errorName;

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_endpoint_id", insertable = false, updatable = false)
    private ApiEndpoint apiEndpoint;

    @Builder
    public ErrorResponse(final Long id, final Long apiEndpointId, final Integer statusCode,
                         final String errorCode, final String errorName, final String errorMessage,
                         final String domainCode, final String categoryCode, final String detailCode,
                         final String description, final Map<String, Object> schemaJson) {
        this.id = id;
        this.apiEndpointId = apiEndpointId;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorName = errorName;
        this.errorMessage = errorMessage;
        this.domainCode = domainCode;
        this.categoryCode = categoryCode;
        this.detailCode = detailCode;
        this.description = description;
        this.schemaJson = schemaJson;
    }
}
