# Swagger MCP Server

Backend API의 Swagger 문서를 MCP 서버로 제공하여, 클라이언트 개발자가 Claude Code에서 **자연어로 API를 검색**하고 **완전한 네트워킹 코드를 자동 생성**할 수 있도록 하는 시스템입니다.

기존에 Swagger UI를 직접 탐색하며 엔드포인트, 요청/응답 구조를 확인하던 과정을 LLM 기반 조회로 대체해 **API 연동 시간을 10배 단축**(5분 → 30초)합니다.

---

## 🎯 핵심 시나리오

```
개발자: "로그인 API 찾아서 DTO 만들어줘"
    ↓
Claude가 자동으로:
1. searchApiByKeyword("로그인") 호출 → API 찾기
2. getApiDetail(apiId) 호출 → Request/Response 스키마 조회
3. 즉시 Kotlin/Swift DTO 코드 생성 + 네트워크 호출 코드 작성
```

**결과**: 5분 걸리던 작업이 30초 만에 완료, 타입 불일치 제로

---

## ✨ 주요 기능

### 1. Swagger 파싱 및 DB 저장
- Backend 서버의 OpenAPI 3.0 JSON 다운로드 및 파싱
- `$ref` Resolution: `#/components/schemas/LoginRequest` → 실제 schema 구조로 변환
- API 엔드포인트, Request/Response/Error 스키마를 MySQL에 구조화 저장
- NDGL 에러 코드 형식 파싱: `DOMAIN-CATEGORY-DETAIL`

### 2. MCP Tools 제공 (Spring AI MCP)
- **searchApiByKeyword**: 키워드로 API 검색 (path, summary, tags)
- **getApiDetail**: API 상세 정보 조회 (Request, Response, Error 포함)
- **getRequestFormat**: Request DTO 스키마 반환
- **getResponseFormat**: Success Response DTO 스키마 반환
- **getErrorFormats**: Error Response 목록 반환

### 3. AI 친화적 응답 포맷
- DB 저장 포맷 → AI가 이해하기 쉬운 구조로 변환 (SchemaFormatter)
- 중첩 객체, 배열 타입, Required/Optional 필드 명확하게 구분
- Example 값 제공으로 즉시 코드 생성 가능

### 4. 동기화 시스템
- Backend API 변경사항 자동 감지 및 재파싱
- Swagger JSON URL 기반 동기화

---

## 🏗️ 아키텍처

### 4-Layer Architecture

```
Controller → Facade → Service → Repository
```

- **Controller**: HTTP 요청/응답 처리, Facade만 의존
- **Facade**: 여러 Service 조합 (orchestration)
- **Service**: 단일 책임 원칙에 따른 세분화된 비즈니스 로직
- **Repository**: Spring Data JPA 기반 데이터 접근

### Swagger 파싱 플로우

```
SwaggerController
    ↓
SwaggerFacade
    ↓
SwaggerParserService (5개 Parser 조합)
    ├─ EndpointBasicInfoParser
    ├─ RequestSchemaParser ($ref resolution)
    ├─ ResponseSchemaParser ($ref resolution)
    ├─ ErrorResponseParser ($ref resolution, 에러 코드 파싱)
    └─ TagParser
    ↓
Repository (DB 저장)
```

### MCP 조회 플로우

```
Claude Code
    ↓ (SSE)
SwaggerMcpTools (@McpTool)
    ↓
ApiSearchService
    ↓
SchemaFormatter (DB 포맷 → AI 친화적 포맷)
    ↓
Repository (DB 조회)
```

---

## 🛠️ 기술 스택

| 카테고리 | 기술 |
|---------|------|
| Framework | Spring Boot 3.5.9 |
| Language | Java 21 |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA |
| MCP | Spring AI MCP 1.1.2 (SSE 기반) |
| Parser | Jackson, Swagger Parser 2.1.37 |
| Utility | Lombok |

---

## 📋 MCP Tools 명세

### 1. `searchApiByKeyword(keyword: String)`
**설명**: 키워드로 API 검색
**검색 대상**: path, summary, description, tags
**반환**: `List<ApiSummary>` (id, method, path, summary, tags)

**예시**:
```json
searchApiByKeyword("로그인")
→ [
    {
      "id": 1,
      "method": "POST",
      "path": "/api/v1/auth/login",
      "summary": "사용자 로그인",
      "tags": ["인증"]
    }
  ]
```

### 2. `getApiDetail(apiId: Long)`
**설명**: API 상세 정보 조회 (Request, Response, Error 포함)
**반환**: `ApiDetailForAI`

**응답 구조**:
```json
{
  "id": 1,
  "method": "POST",
  "path": "/api/v1/auth/login",
  "summary": "사용자 로그인",
  "description": "이메일과 비밀번호로 로그인",
  "tags": ["인증"],
  "request": {
    "body": [
      {"name": "email", "type": "string", "required": true, "example": "user@example.com"},
      {"name": "password", "type": "string", "required": true, "example": "password123"}
    ],
    "parameters": []
  },
  "responses": {
    "200": {
      "statusCode": 200,
      "description": "로그인 성공",
      "fields": [
        {"name": "accessToken", "type": "string", "required": true},
        {"name": "refreshToken", "type": "string", "required": true}
      ]
    }
  },
  "errors": {
    "401": {
      "statusCode": 401,
      "description": "인증 실패",
      "errorCode": "AUTH-LOGIN-INVALID_CREDENTIALS"
    }
  }
}
```

### 3. `getRequestFormat(apiId: Long)`
**설명**: Request Body + Parameters 조회
**반환**: `RequestForAI`

### 4. `getResponseFormat(apiId: Long)`
**설명**: 상태 코드별 Success Response 조회
**반환**: `Map<Integer, ResponseForAI>`

### 5. `getErrorFormats(apiId: Long)`
**설명**: 상태 코드별 Error Response 조회
**반환**: `Map<Integer, ErrorForAI>`

---

## 🚀 빌드 및 실행

### 사전 요구사항
- Java 21+
- MySQL 8.0
- Docker (선택)

### 환경 변수 설정

`.env` 파일 또는 시스템 환경 변수로 설정:

```bash
SPRING_DATASOURCE_URL=your_datasource_url
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
```

### 빌드

```bash
./gradlew clean build
```

### 실행

```bash
./gradlew bootRun
```

### Docker로 MySQL 실행 (로컬 개발)

```bash
docker compose up -d
```

---

## 📡 API 엔드포인트

### 1. Swagger 동기화
```http
POST /api/swagger/sync
Content-Type: application/json

{
  "swaggerUrl": "https://backend-api.example.com/v3/api-docs"
}
```

**응답**:
```json
{
  "syncedAt": "2026-02-07T10:30:00",
  "totalEndpoints": 42,
  "message": "Swagger 동기화 완료"
}
```

### 2. MCP 엔드포인트 (SSE)
```http
GET /mcp/sse
Accept: text/event-stream
```

Claude Code가 이 엔드포인트를 통해 MCP Tools를 호출합니다.

---

## 📁 프로젝트 구조

```
src/main/java/com/ndgl/swaggermcp/
├── controller/          # HTTP 요청 처리
├── facade/              # Service 조합 (orchestration)
├── service/
│   ├── swagger/         # Swagger 파싱 관련
│   │   ├── SwaggerParserService.java
│   │   ├── SwaggerFetchService.java
│   │   └── parser/      # 5개 Parser
│   └── ApiSearchService.java  # MCP Tool 비즈니스 로직
├── formatter/           # DB 포맷 → AI 친화적 포맷 변환
│   └── SchemaFormatter.java
├── mcp/                 # MCP Tools 정의
│   └── SwaggerMcpTools.java
├── dto/
│   ├── parser/          # 파싱 결과 DTO (내부 사용)
│   ├── facade/          # Facade 응답 DTO
│   └── mcp/             # MCP Tool 응답 DTO (AI 친화적)
├── entity/              # JPA Entity
└── repository/          # Spring Data JPA Repository
```

---

## 🎨 왜 SSE(Server-Sent Events)인가?

MCP 프로토콜은 **서버 → 클라이언트 단방향 스트리밍**이 주요 패턴입니다.

| 전송 방식 | 연결 유지 | 스트리밍 | MCP 적합도 | 이유 |
|----------|----------|---------|-----------|------|
| **SSE** ✅ | O | O | 최적 | HTTP 기반, 단방향 충분, Spring AI 기본 지원 |
| HTTP (stateless) | X | X | 비효율 | 매번 연결 맺고 끊음, 실시간 Tool 응답 불가 |
| WebSocket | O | O | 과도 | 양방향 필요 없음, SSE가 더 단순 |

Spring AI MCP는 SSE를 기본 전송 방식으로 채택하여, MCP 프로토콜 요구사항(실시간 Tool 응답, 진행 상황 업데이트)을 충족하면서도 구현이 단순하고 HTTP 기반이라 인프라 친화적입니다.

