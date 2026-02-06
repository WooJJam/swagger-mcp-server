package com.ndgl.swaggermcp.formatter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.swaggermcp.dto.mcp.FieldInfo;
import com.ndgl.swaggermcp.dto.mcp.ParameterInfo;
import com.ndgl.swaggermcp.entity.Parameter;
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
public class SchemaFormatter {

    private final ObjectMapper objectMapper;

    /**
     * Schema JSON을 AI 친화적 포맷으로 변환
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
            // 1. Schema를 Map으로 변환
            final Map<String, Object> schemaMap = convertToMap(schemaJson);

            // 2. Example을 Map으로 변환
            final Map<String, Object> exampleMap = exampleJson != null ? convertToMap(exampleJson) : Collections.emptyMap();

            // 3. Properties 추출
            final Map<String, Object> properties = extractProperties(schemaMap);

            // 4. Required 필드 추출
            final List<String> required = extractRequired(schemaMap);

            // 5. 각 필드를 FieldInfo로 변환
            final Map<String, FieldInfo> result = new LinkedHashMap<>();
            for (final Map.Entry<String, Object> entry : properties.entrySet()) {
                final String fieldName = entry.getKey();
                final Map<String, Object> fieldSchema = convertToMap(entry.getValue());

                final FieldInfo fieldInfo = createFieldInfo(
                    fieldSchema,
                    required.contains(fieldName),
                    exampleMap.get(fieldName)
                );

                result.put(fieldName, fieldInfo);
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

        // 기타 타입은 ObjectMapper로 변환 시도
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
     * Schema에서 required 필드 리스트 추출
     */
    private List<String> extractRequired(final Map<String, Object> schemaMap) {
        final Object required = schemaMap.get("required");
        if (required instanceof List) {
            return ((List<?>) required).stream()
                .map(Object::toString)
                .toList();
        }
        return Collections.emptyList();
    }

    /**
     * FieldInfo 생성
     */
    private FieldInfo createFieldInfo(
        final Map<String, Object> fieldSchema,
        final boolean required,
        final Object example
    ) {
        final String type = getStringValue(fieldSchema, "type");
        final String format = getStringValue(fieldSchema, "format");
        final String description = getStringValue(fieldSchema, "description");

        // example이 fieldSchema 내부에 있을 수도 있음
        final Object finalExample = example != null ? example : fieldSchema.get("example");

        return new FieldInfo(type, format, required, description, finalExample);
    }

    /**
     * Map에서 String 값 안전하게 추출
     */
    private String getStringValue(final Map<String, Object> map, final String key) {
        final Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
