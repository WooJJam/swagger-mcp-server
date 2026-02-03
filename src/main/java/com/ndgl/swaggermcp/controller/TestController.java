package com.ndgl.swaggermcp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.swaggermcp.service.SwaggerFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final SwaggerFetchService swaggerFetchService;
    private final ObjectMapper objectMapper;

    /**
     * Phase 2.1 테스트: 로컬 샘플 파일 읽기 테스트
     */
    @GetMapping("/sample-file")
    public ResponseEntity<Map<String, Object>> testSampleFile() {
        log.info("=== Testing Phase 2.1: Sample File Test ===");
        Map<String, Object> result = new HashMap<>();

        try {
            // 샘플 파일 읽기
            ClassPathResource resource = new ClassPathResource("sample-swagger.json");
            JsonNode sampleJson = objectMapper.readTree(resource.getInputStream());

            // 유효성 검증
            boolean isValid = swaggerFetchService.validateSwaggerJson(sampleJson);
            String version = swaggerFetchService.extractSwaggerVersion(sampleJson);

            // 결과 생성
            result.put("success", true);
            result.put("message", "샘플 파일 읽기 성공");
            result.put("isValid", isValid);
            result.put("openapiVersion", version);
            result.put("title", sampleJson.path("info").path("title").asText());
            result.put("apiCount", sampleJson.path("paths").size());

            // paths 목록
            Map<String, Object> paths = new HashMap<>();
            sampleJson.path("paths").fields().forEachRemaining(entry -> {
                String path = entry.getKey();
                JsonNode methods = entry.getValue();
                paths.put(path, methods.fieldNames());
            });
            result.put("paths", paths);

            log.info("✅ Sample file test passed: {} APIs found", sampleJson.path("paths").size());
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            log.error("❌ Sample file test failed", e);
            result.put("success", false);
            result.put("message", "샘플 파일 읽기 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Phase 2.1 테스트: 외부 URL에서 Swagger JSON 다운로드 테스트
     */
    @GetMapping("/fetch-url")
    public ResponseEntity<Map<String, Object>> testFetchUrl(
            @RequestParam(defaultValue = "https://petstore3.swagger.io/api/v3/openapi.json") String url
    ) {
        log.info("=== Testing Phase 2.1: Fetch URL Test ===");
        log.info("Fetching from: {}", url);
        Map<String, Object> result = new HashMap<>();

        try {
            // URL에서 Swagger JSON 다운로드
            JsonNode swaggerJson = swaggerFetchService.fetchSwaggerJson(url);

            // 유효성 검증
            boolean isValid = swaggerFetchService.validateSwaggerJson(swaggerJson);
            String version = swaggerFetchService.extractSwaggerVersion(swaggerJson);

            // 결과 생성
            result.put("success", true);
            result.put("message", "URL에서 Swagger JSON 다운로드 성공");
            result.put("url", url);
            result.put("isValid", isValid);
            result.put("openapiVersion", version);
            result.put("title", swaggerJson.path("info").path("title").asText());
            result.put("apiCount", swaggerJson.path("paths").size());

            log.info("✅ URL fetch test passed: {} APIs found", swaggerJson.path("paths").size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ URL fetch test failed", e);
            result.put("success", false);
            result.put("message", "URL 다운로드 실패: " + e.getMessage());
            result.put("url", url);
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Phase 2.1 테스트: Swagger JSON 유효성 검증 테스트
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> testValidation(@RequestBody String jsonString) {
        log.info("=== Testing Phase 2.1: Validation Test ===");
        Map<String, Object> result = new HashMap<>();

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            // 유효성 검증
            boolean isValid = swaggerFetchService.validateSwaggerJson(jsonNode);
            String version = swaggerFetchService.extractSwaggerVersion(jsonNode);

            result.put("success", true);
            result.put("isValid", isValid);
            result.put("openapiVersion", version);

            if (isValid) {
                result.put("message", "유효한 Swagger JSON입니다");
                result.put("title", jsonNode.path("info").path("title").asText());
                result.put("apiCount", jsonNode.path("paths").size());
                log.info("✅ Validation test passed");
            } else {
                result.put("message", "유효하지 않은 Swagger JSON입니다");
                log.warn("⚠️ Validation test failed: Invalid JSON");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Validation test failed", e);
            result.put("success", false);
            result.put("message", "JSON 파싱 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Phase 2.1 전체 테스트 실행
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> runAllTests() {
        log.info("=== Running All Phase 2.1 Tests ===");
        Map<String, Object> result = new HashMap<>();

        // Test 1: 샘플 파일 읽기
        ResponseEntity<Map<String, Object>> test1 = testSampleFile();
        result.put("test1_sampleFile", test1.getBody());

        // Test 2: 외부 URL (Petstore 예제)
        ResponseEntity<Map<String, Object>> test2 = testFetchUrl("https://petstore3.swagger.io/api/v3/openapi.json");
        result.put("test2_petstoreUrl", test2.getBody());

        // 전체 결과
        boolean allPassed = test1.getStatusCode().is2xxSuccessful()
                && test2.getStatusCode().is2xxSuccessful();

        result.put("allTestsPassed", allPassed);
        result.put("message", allPassed ? "✅ 모든 테스트 통과!" : "❌ 일부 테스트 실패");

        log.info("=== Phase 2.1 Test Results: {} ===", allPassed ? "PASSED" : "FAILED");
        return ResponseEntity.ok(result);
    }

    /**
     * 샘플 Swagger JSON 상세 구조 확인
     */
    @GetMapping("/sample-structure")
    public ResponseEntity<Map<String, Object>> testSampleStructure() {
        log.info("=== Testing Sample Swagger Structure ===");
        Map<String, Object> result = new HashMap<>();

        try {
            ClassPathResource resource = new ClassPathResource("sample-swagger.json");
            JsonNode sampleJson = objectMapper.readTree(resource.getInputStream());

            result.put("success", true);

            // 기본 정보
            result.put("openapi", sampleJson.path("openapi").asText());
            result.put("title", sampleJson.path("info").path("title").asText());
            result.put("version", sampleJson.path("info").path("version").asText());

            // Tags
            Map<String, String> tags = new HashMap<>();
            sampleJson.path("tags").forEach(tag -> {
                tags.put(tag.path("name").asText(), tag.path("description").asText());
            });
            result.put("tags", tags);

            // Paths 상세
            Map<String, Object> pathsDetail = new HashMap<>();
            sampleJson.path("paths").fields().forEachRemaining(pathEntry -> {
                String path = pathEntry.getKey();
                JsonNode methods = pathEntry.getValue();

                Map<String, Object> methodDetail = new HashMap<>();
                methods.fields().forEachRemaining(methodEntry -> {
                    String method = methodEntry.getKey();
                    JsonNode operation = methodEntry.getValue();

                    Map<String, Object> operationDetail = new HashMap<>();
                    operationDetail.put("summary", operation.path("summary").asText());
                    operationDetail.put("operationId", operation.path("operationId").asText());

                    // Response status codes
                    Map<String, String> responses = new HashMap<>();
                    operation.path("responses").fields().forEachRemaining(respEntry -> {
                        responses.put(respEntry.getKey(), respEntry.getValue().path("description").asText());
                    });
                    operationDetail.put("responses", responses);

                    methodDetail.put(method, operationDetail);
                });

                pathsDetail.put(path, methodDetail);
            });
            result.put("pathsDetail", pathsDetail);

            log.info("✅ Sample structure test passed");
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            log.error("❌ Sample structure test failed", e);
            result.put("success", false);
            result.put("message", "구조 확인 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
