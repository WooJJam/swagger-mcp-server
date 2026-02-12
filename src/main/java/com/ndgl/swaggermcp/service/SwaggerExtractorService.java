package com.ndgl.swaggermcp.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwaggerExtractorService {

	/**
	 * Swagger 버전 추출
	 * @param swaggerJson Swagger JSON
	 * @return OpenAPI 버전 (예: "3.0.1")
	 */
	public String extractVersion(final JsonNode swaggerJson) {
		final JsonNode openapi = swaggerJson.path("openapi");
		if (!openapi.isMissingNode()) {
			return openapi.asText();
		}

		final JsonNode swagger = swaggerJson.path("swagger");
		if (!swagger.isMissingNode()) {
			return swagger.asText();
		}

		log.warn("Swagger 버전 정보를 찾을 수 없습니다");
		return "unknown";
	}
}
