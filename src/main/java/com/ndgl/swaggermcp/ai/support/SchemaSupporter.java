package com.ndgl.swaggermcp.ai.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.swaggermcp.ai.dto.FieldInfo;
import com.ndgl.swaggermcp.ai.dto.ParameterInfo;
import com.ndgl.swaggermcp.persistence.entity.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Schema 포맷 변환기
 * DB 저장 포맷 → AI 친화적 응답 포맷
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaSupporter {

    private final ObjectMapper objectMapper;

    /**
     * Schema JSON을 AI 친화적 포맷으로 변환
     * DB에 저장된 스키마는 enrichSchemaWithRequired()를 통해
     * 모든 깊이의 필드에 required 플래그가 이미 인라인된 상태다.
     *
     * @param schemaJson Schema JSON (Map 또는 String)
     * @param exampleJson Example JSON (Map 또는 null)
     * @return Map<필드명, FieldInfo>
     */
    public Map<String, FieldInfo> formatSchema(final Object schemaJson, final Object exampleJson) {
        if (schemaJson == null) {
            return Collections.emptyMap();
        }

        try {
            final Map<String, Object> schemaMap = convertToMap(schemaJson);
            final Map<String, Object> exampleMap = exampleJson != null ? convertToMap(exampleJson) : Collections.emptyMap();
            final Map<String, Object> properties = extractProperties(schemaMap);

            final Map<String, FieldInfo> result = new LinkedHashMap<>();
            for (final Map.Entry<String, Object> entry : properties.entrySet()) {
                final String fieldName = entry.getKey();
                final Map<String, Object> fieldSchema = convertToMap(entry.getValue());

                result.put(fieldName, createFieldInfo(fieldSchema, exampleMap.get(fieldName)));
            }

            return result;
        } catch (Exception e) {
            log.error("Schema 포맷 변환 실패", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Parameter 엔티티 리스트를 ParameterInfo 리스트로 변환
     */
    public List<ParameterInfo> formatParameters(final List<Parameter> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Collections.emptyList();
        }

        return parameters.stream()
            .map(param -> new ParameterInfo(
                param.getName(),
                param.getIn(),
                param.getType(),
                param.getFormat(),
                param.getRequired(),
                param.getDescription()
            ))
            .toList();
    }

    /**
     * FieldInfo 생성 (중첩 object/array 재귀 처리)
     * required 값은 enrichSchemaWithRequired()에서 이미 각 필드에 인라인되어 있으므로
     * fieldSchema에서 직접 읽는다.
     */
    private FieldInfo createFieldInfo(final Map<String, Object> fieldSchema, final Object example) {
        final String type = getStringValue(fieldSchema, "type");
        final String format = getStringValue(fieldSchema, "format");
        final String description = getStringValue(fieldSchema, "description");
        final boolean required = Boolean.TRUE.equals(fieldSchema.get("required"));
        final Object finalExample = example != null ? example : fieldSchema.get("example");

        // 중첩 object 타입 처리: 내부 properties 재귀 변환
        Map<String, FieldInfo> nestedProperties = null;
        if ("object".equals(type) && fieldSchema.containsKey("properties")) {
            nestedProperties = formatNestedProperties(fieldSchema);
        }

        // 배열 타입 처리: items 내부 구조 재귀 변환
        FieldInfo itemsFieldInfo = null;
        if ("array".equals(type) && fieldSchema.containsKey("items")) {
            final Map<String, Object> itemsSchema = convertToMap(fieldSchema.get("items"));
            itemsFieldInfo = createFieldInfo(itemsSchema, null);
        }

        return new FieldInfo(type, format, required, description, finalExample, nestedProperties, itemsFieldInfo);
    }

    /**
     * 중첩 object의 properties를 재귀적으로 FieldInfo Map으로 변환
     */
    private Map<String, FieldInfo> formatNestedProperties(final Map<String, Object> schemaMap) {
        final Map<String, Object> properties = extractProperties(schemaMap);
        if (properties.isEmpty()) {
            return null;
        }

        final Map<String, FieldInfo> result = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : properties.entrySet()) {
            final Map<String, Object> fieldSchema = convertToMap(entry.getValue());
            result.put(entry.getKey(), createFieldInfo(fieldSchema, null));
        }

        return result;
    }

    /**
     * Object를 Map으로 변환
     */
    private Map<String, Object> convertToMap(final Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }

        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }

        if (obj instanceof String) {
            try {
                return objectMapper.readValue((String) obj, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("JSON String을 Map으로 변환 실패: {}", obj, e);
                return Collections.emptyMap();
            }
        }

        try {
            final String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Object를 Map으로 변환 실패: {}", obj, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Schema에서 properties 추출
     */
    private Map<String, Object> extractProperties(final Map<String, Object> schemaMap) {
        final Object properties = schemaMap.get("properties");
        if (properties instanceof Map) {
            return (Map<String, Object>) properties;
        }
        return Collections.emptyMap();
    }

    /**
     * Map에서 String 값 안전하게 추출
     */
    private String getStringValue(final Map<String, Object> map, final String key) {
        final Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
