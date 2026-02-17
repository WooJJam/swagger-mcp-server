package com.ndgl.swaggermcp.sync.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonSchemaParsingSupport {

    private final ObjectMapper objectMapper;

    public JsonNode selectContentNode(final JsonNode content) {
        if (content == null || content.isMissingNode() || !content.isObject()) {
            return MissingNode.getInstance();
        }

        final JsonNode jsonContent = content.path("application/json");
        if (!jsonContent.isMissingNode()) {
            return jsonContent;
        }

        final Iterator<String> contentTypes = content.fieldNames();
        if (!contentTypes.hasNext()) {
            return MissingNode.getInstance();
        }

        final String firstContentType = contentTypes.next();
        return content.path(firstContentType);
    }

    public ExampleData extractExample(final JsonNode jsonContent, final JsonNode resolvedSchema) {
        if (jsonContent == null || jsonContent.isMissingNode()) {
            return ExampleData.empty();
        }

        // 1. content level의 example 먼저 확인
        final JsonNode example = jsonContent.path("example");
        if (!example.isMissingNode()) {
            return new ExampleData(example, null, null);
        }

        // 2. content level의 examples 확인
        final JsonNode examples = jsonContent.path("examples");
        if (examples.isObject()) {
            final Iterator<String> exampleNames = examples.fieldNames();
            if (exampleNames.hasNext()) {
                final String firstName = exampleNames.next();
                final JsonNode exampleEntry = examples.path(firstName);
                final JsonNode value = exampleEntry.path("value");
                final JsonNode valueNode = value.isMissingNode() ? exampleEntry : value;

                return new ExampleData(
                        valueNode,
                        firstName,
                        exampleEntry.path("description").asText("")
                );
            }
        }

        // 3. schema 내부의 properties에서 example 추출 (resolvedSchema 사용)
        if (resolvedSchema != null && !resolvedSchema.isMissingNode()) {
            final JsonNode exampleFromSchema = extractExamplesFromSchema(resolvedSchema);
            if (exampleFromSchema != null && !exampleFromSchema.isNull()) {
                return new ExampleData(exampleFromSchema, null, null);
            }
        }

        return ExampleData.empty();
    }

    public String convertToJsonString(final JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("JsonNode를 문자열로 변환 실패", e);
            return null;
        }
    }

    public String extractDtoNameFromRef(final String ref) {
        if (ref == null || ref.isEmpty()) {
            return "";
        }

        final int lastSlashIndex = ref.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < ref.length() - 1) {
            return ref.substring(lastSlashIndex + 1);
        }

        return ref;
    }

    /**
     * $ref를 resolve하여 실제 schema 반환
     *
     * @param swaggerJson 전체 Swagger JSON
     * @param schemaRef $ref 값 (예: #/components/schemas/CreateUserTravelRequest)
     * @return 실제 schema 또는 null
     */
    private JsonNode resolveSchemaRef(final JsonNode swaggerJson, final String schemaRef) {
        if (schemaRef == null || schemaRef.isEmpty() || !schemaRef.startsWith("#/")) {
            return null;
        }

        // #/components/schemas/CreateUserTravelRequest → components/schemas/CreateUserTravelRequest
        final String path = schemaRef.substring(2); // #/ 제거
        final String[] parts = path.split("/");

        JsonNode current = swaggerJson;
        for (final String part : parts) {
            if (current == null || current.isMissingNode()) {
                log.warn("Schema ref를 resolve할 수 없습니다: {}", schemaRef);
                return null;
            }
            current = current.path(part);
        }

        if (current.isMissingNode()) {
            log.warn("Schema를 찾을 수 없습니다: {}", schemaRef);
            return null;
        }

        return current;
    }

    /**
     * Schema 내부의 모든 $ref를 재귀적으로 resolve하여 완전한 schema 반환
     * 원본 schema는 수정하지 않고 deepCopy한 복사본에서 작업
     *
     * @param swaggerJson 전체 Swagger JSON
     * @param schema resolve 대상 schema
     * @return 모든 $ref가 resolve된 schema (deepCopy본)
     */
    public JsonNode resolveAllRefs(final JsonNode swaggerJson, final JsonNode schema) {
        if (schema == null || schema.isMissingNode()) {
            return schema;
        }

        final Set<String> visited = new LinkedHashSet<>();
        final JsonNode copied = schema.deepCopy();

        // 최상위 노드 자체가 $ref인 경우 처리
        final JsonNode resolved = resolveIfRef(swaggerJson, copied, visited);
        if (resolved != copied) {
            return resolved;
        }

        resolveAllRefsRecursive(swaggerJson, copied, visited);
        return copied;
    }

    /**
     * 노드 내부의 모든 $ref를 재귀적으로 탐색하여 resolve
     *
     * @param swaggerJson 전체 Swagger JSON
     * @param node 탐색 대상 노드 (이미 deepCopy된 상태)
     * @param visited 순환 참조 감지를 위한 DFS 경로 추적 Set
     */
    private void resolveAllRefsRecursive(final JsonNode swaggerJson, final JsonNode node, final Set<String> visited) {
        if (node == null || node.isMissingNode() || !node.isObject()) {
            return;
        }

        final ObjectNode objectNode = (ObjectNode) node;

        // 1. properties 내부 각 필드의 $ref 처리
        final JsonNode properties = objectNode.path("properties");
        if (properties.isObject()) {
            final ObjectNode propertiesNode = (ObjectNode) properties;
            final var fieldNameList = new java.util.ArrayList<String>();
            propertiesNode.fieldNames().forEachRemaining(fieldNameList::add);

            for (final String fieldName : fieldNameList) {
                final JsonNode fieldSchema = propertiesNode.get(fieldName);
                final JsonNode resolvedField = resolveIfRef(swaggerJson, fieldSchema, visited);
                if (resolvedField != fieldSchema) {
                    propertiesNode.set(fieldName, resolvedField);
                }
                resolveAllRefsRecursive(swaggerJson, resolvedField, visited);
            }
        }

        // 2. items (array 타입) 내부 $ref 처리
        resolveChildRef(swaggerJson, objectNode, "items", visited);

        // 3. allOf, oneOf, anyOf 배열 내부 $ref 처리
        resolveRefsInCompositionArray(swaggerJson, objectNode, "allOf", visited);
        resolveRefsInCompositionArray(swaggerJson, objectNode, "oneOf", visited);
        resolveRefsInCompositionArray(swaggerJson, objectNode, "anyOf", visited);

        // 4. additionalProperties 내부 $ref 처리
        resolveChildRef(swaggerJson, objectNode, "additionalProperties", visited);
    }

    /**
     * allOf/oneOf/anyOf 배열의 각 요소에 대해 $ref resolve 수행
     */
    private void resolveRefsInCompositionArray(final JsonNode swaggerJson, final ObjectNode parentNode,
                                                final String compositionKey, final Set<String> visited) {
        final JsonNode compositionArray = parentNode.path(compositionKey);
        if (!compositionArray.isArray()) {
            return;
        }

        final ArrayNode arrayNode = (ArrayNode) compositionArray;
        for (int i = 0; i < arrayNode.size(); i++) {
            final JsonNode element = arrayNode.get(i);
            final JsonNode resolvedElement = resolveIfRef(swaggerJson, element, visited);
            if (resolvedElement != element) {
                arrayNode.set(i, resolvedElement);
            }
            resolveAllRefsRecursive(swaggerJson, resolvedElement, visited);
        }
    }

    /**
     * 부모 노드의 특정 자식 키에 대해 $ref resolve 수행
     */
    private void resolveChildRef(final JsonNode swaggerJson, final ObjectNode parentNode,
                                  final String childKey, final Set<String> visited) {
        final JsonNode child = parentNode.path(childKey);
        if (child.isObject()) {
            final JsonNode resolved = resolveIfRef(swaggerJson, child, visited);
            if (resolved != child) {
                parentNode.set(childKey, resolved);
            }
            resolveAllRefsRecursive(swaggerJson, resolved, visited);
        }
    }

    /**
     * 노드가 $ref를 가지고 있으면 resolve하여 실제 schema 반환
     * 순환 참조 감지: DFS 경로 기반 visited Set 사용 (형제 노드의 같은 $ref는 허용, 조상-자손 순환만 차단)
     *
     * @param swaggerJson 전체 Swagger JSON
     * @param node 검사 대상 노드
     * @param visited 순환 참조 감지를 위한 DFS 경로 추적 Set
     * @return resolve된 노드 (deepCopy본) 또는 원본 노드 ($ref가 없는 경우)
     */
    private JsonNode resolveIfRef(final JsonNode swaggerJson, final JsonNode node, final Set<String> visited) {
        if (node == null || !node.isObject() || !node.has("$ref")) {
            return node;
        }

        final String ref = node.get("$ref").asText();

        // 순환 참조 감지: 현재 DFS 경로에 이미 존재하면 순환
        if (visited.contains(ref)) {
            log.warn("순환 참조 감지: {}", ref);
            final ObjectNode circularMarker = objectMapper.createObjectNode();
            circularMarker.put("_circular", true);
            circularMarker.put("$ref", ref);
            return circularMarker;
        }

        final JsonNode resolvedOriginal = resolveSchemaRef(swaggerJson, ref);
        if (resolvedOriginal == null) {
            log.warn("$ref resolve 실패, 원본 노드 반환: {}", ref);
            return node;
        }

        final JsonNode resolved = resolvedOriginal.deepCopy();

        // DFS 경로에 추가 후 재귀 처리, 완료 후 제거
        visited.add(ref);
        resolveAllRefsRecursive(swaggerJson, resolved, visited);
        visited.remove(ref);

        return resolved;
    }

    /**
     * Schema의 required 배열 정보를 각 property에 포함시켜 enriched schema 생성
     *
     * @param schema 원본 OpenAPI schema
     * @return required 정보가 각 필드에 포함된 schema
     */
    public JsonNode enrichSchemaWithRequired(final JsonNode schema) {
        if (schema == null || schema.isMissingNode()) {
            return schema;
        }

        // schema를 복사하여 수정 (원본 보존)
        final var enrichedSchema = schema.deepCopy();

        final JsonNode properties = enrichedSchema.path("properties");
        if (properties.isMissingNode() || !properties.isObject()) {
            return enrichedSchema;
        }

        // required 배열 추출
        final JsonNode requiredArray = enrichedSchema.path("required");
        final var requiredFields = new HashSet<String>();
        if (requiredArray.isArray()) {
            requiredArray.forEach(field -> requiredFields.add(field.asText()));
        }

        // properties의 각 필드에 required 정보 추가
        final ObjectNode propertiesObject = (ObjectNode) properties;
        for (final var entry : propertiesObject.properties()) {
            String fieldName = entry.getKey();
            final JsonNode fieldSchema = entry.getValue();

            if (fieldSchema instanceof ObjectNode objectNode) {
                objectNode.put("required", requiredFields.contains(fieldName));
            }
        }

        return enrichedSchema;
    }

    /**
     * Schema의 properties에서 example들을 추출하여 하나의 객체로 조립
     *
     * @param schema OpenAPI schema 객체
     * @return example 객체 (JsonNode) 또는 null
     */
    private JsonNode extractExamplesFromSchema(final JsonNode schema) {
        if (schema == null || schema.isMissingNode()) {
            return null;
        }

        final JsonNode properties = schema.path("properties");
        if (properties.isMissingNode() || !properties.isObject()) {
            return null;
        }

        final var exampleObject = objectMapper.createObjectNode();
        boolean hasAnyExample = false;

        for (final Iterator<Map.Entry<String, JsonNode>> it = properties.fields(); it.hasNext(); ) {
            final Map.Entry<String, JsonNode> entry = it.next();
            final String fieldName = entry.getKey();
            final JsonNode fieldSchema = entry.getValue();

            final JsonNode fieldExample = fieldSchema.path("example");
            if (!fieldExample.isMissingNode()) {
                exampleObject.set(fieldName, fieldExample);
                hasAnyExample = true;
            }
        }

        return hasAnyExample ? exampleObject : null;
    }

    public record ExampleData(JsonNode value, String name, String description) {
        private static ExampleData empty() {
            return new ExampleData(null, null, null);
        }
    }
}

