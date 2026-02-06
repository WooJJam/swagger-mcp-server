package com.ndgl.swaggermcp.controller;

import com.ndgl.swaggermcp.controller.request.ParseRequest;
import com.ndgl.swaggermcp.dto.facade.SwaggerSyncResult;
import com.ndgl.swaggermcp.facade.SwaggerFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Swagger 동기화 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/swagger")
@RequiredArgsConstructor
public class SyncController {

    private final SwaggerFacade swaggerFacade;

    /**
     * Swagger JSON을 파싱하여 DB에 저장
     *
     * @param request 파싱 요청 (Swagger URL 포함)
     * @return 동기화 결과
     */
    @PostMapping("/sync")
    public ResponseEntity<SwaggerSyncResult> parseSwagger(
            @RequestBody final ParseRequest request
    ) {
        log.info("Swagger 파싱 요청: {}", request.url());

        final SwaggerSyncResult result = swaggerFacade.syncSwagger(request.url());

        if (result.success()) {
            log.info("Swagger 파싱 성공: {} 엔드포인트", result.totalEndpoints());
            return ResponseEntity.ok(result);
        } else {
            log.error("Swagger 파싱 실패: {}", result.message());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
