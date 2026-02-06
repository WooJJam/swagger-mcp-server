package com.ndgl.swaggermcp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parameters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_endpoint_id", nullable = false)
    private Long apiEndpointId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "in_type", nullable = false, length = 20)
    private String in;

    @Column(name = "required", nullable = false)
    private Boolean required;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "format", length = 50)
    private String format;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_endpoint_id", insertable = false, updatable = false)
    private ApiEndpoint apiEndpoint;

    @Builder
    public Parameter(final Long id, final Long apiEndpointId, final String name,
                     final String in, final Boolean required, final String type,
                     final String format, final String description) {
        this.id = id;
        this.apiEndpointId = apiEndpointId;
        this.name = name;
        this.in = in;
        this.required = required;
        this.type = type;
        this.format = format;
        this.description = description;
    }
}