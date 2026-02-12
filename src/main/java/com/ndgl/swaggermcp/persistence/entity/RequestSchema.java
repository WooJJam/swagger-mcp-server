package com.ndgl.swaggermcp.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "request_schemas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_endpoint_id", nullable = false)
    private Long apiEndpointId;

    @Column(name = "dto_name", length = 255)
    private String dtoName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_json", nullable = false, columnDefinition = "JSON")
    private Map<String, Object> schemaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "example_json", columnDefinition = "JSON")
    private Map<String, Object> exampleJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_endpoint_id", insertable = false, updatable = false)
    private ApiEndpoint apiEndpoint;

    @Builder
    public RequestSchema(final Long id, final Long apiEndpointId, final String dtoName,
                         final Map<String, Object> schemaJson, final Map<String, Object> exampleJson) {
        this.id = id;
        this.apiEndpointId = apiEndpointId;
        this.dtoName = dtoName;
        this.schemaJson = schemaJson;
        this.exampleJson = exampleJson;
    }
}
