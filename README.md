# Swagger MCP Server

Backend API의 Swagger 문서를 MCP 서버로 제공하여, 클라이언트 개발자가 Claude Code에서 **자연어로 API를 검색**하고 **완전한 네트워킹 코드를 자동 생성**할 수 있도록 하는 시스템입니다.

기존에 Swagger UI를 직접 탐색하며 엔드포인트, 요청/응답 구조를 확인하던 과정을 LLM 기반 조회로 대체해 단축합니다.

[Swagger MCP Server 만들기 1편 - LLM 기반 API 탐색 자동화 설계](https://woojjam.tistory.com/18)

[Swagger MCP Server 만들기 2편 - Swagger API 파싱](https://woojjam.tistory.com/19)

[Swagger MCP Server 만들기 3편 - DB 저장 설계와 트러블 슈팅](https://woojjam.tistory.com/20)

[Swagger MCP Server 만들기 4편 - Spring AI로 MCP 서버를 구축하고, Claude Code에 연동하기](https://woojjam.tistory.com/21)


## Key Point

```
개발자: "로그인 API 찾아서 DTO 만들어줘"
    ↓
Claude가 자동으로:
1. searchApiByKeyword("로그인") 호출 → API 찾기
2. getApiDetail(apiId) 호출 → Request/Response 스키마 조회
3. 즉시 Kotlin/Swift DTO 코드 생성 + 네트워크 호출 코드 작성
```

## Usage

### 사전 요구사항
- Java 21+
- Docker

### 로컬 실행

```bash
# 1. .env 파일 생성
cp .env.example .env  # 값 채워넣기

# 2. 컨테이너 실행 (MySQL + App)
docker compose up -d

# 3. 또는 로컬 직접 실행
./gradlew bootRun
```

### .env 설정값

```
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=
MYSQL_USER=
MYSQL_PASSWORD=
MCP_ACCESS_TOKEN=
APP_IMAGE=
```

[//]: # (## 📡 API 엔드포인트)

[//]: # ()
[//]: # (### Swagger 동기화)

[//]: # (```http)

[//]: # (POST /api/swagger/sync)

[//]: # (Authorization: Bearer {MCP_ACCESS_TOKEN})

[//]: # (Content-Type: application/json)

[//]: # ()
[//]: # ({)

[//]: # (  "swaggerUrl": "https://backend-api.example.com/v3/api-docs")

[//]: # (})

[//]: # (```)

### MCP 엔드포인트
```http
GET /mcp
Authorization: Bearer {MCP_ACCESS_TOKEN}
```

## Claude Code 연결

```bash
claude mcp add --transport http swagger-mcp http://{SERVER_IP}:{SERVER_PORT}/mcp \
  --header "Authorization: Bearer {MCP_ACCESS_TOKEN}" --scope project
```


## Features

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
- DB 저장 포맷 → AI가 이해하기 쉬운 구조로 변환 (SchemaSupporter)
- 중첩 객체, 배열 타입, Required/Optional 필드 명확하게 구분
- Example 값 제공으로 즉시 코드 생성 가능


## 🏗️ 아키텍처

### 4-Layer Architecture

```
Controller → Facade(UseCase) → Service → Repository
```

- **Controller**: HTTP 요청/응답 처리, UseCase만 의존
- **UseCase(Facade)**: 여러 Service 조합 (orchestration)
- **Service**: 단일 책임 원칙에 따른 세분화된 비즈니스 로직
- **Repository**: Spring Data JPA 기반 데이터 접근

### Swagger 동기화 플로우

```
SyncController
    ↓
SyncSwaggerUseCase (Facade)
    ├─→ SwaggerFetchService     (Swagger JSON 다운로드)
    ├─→ SwaggerExtractorService (메타데이터 추출)
    ├─→ SwaggerParserService    (파싱 오케스트레이션)
    │       ├─ EndpointBasicInfoParser
    │       ├─ RequestSchemaParser  ($ref resolution)
    │       ├─ ResponseSchemaParser ($ref resolution)
    │       ├─ ErrorResponseParser  ($ref resolution, 에러 코드 파싱)
    │       └─ TagParser
    └─→ SwaggerSyncService      (DB 저장)
```

### MCP 조회 플로우

```
Claude Code
    ↓
SwaggerMcpTools (@Tool)
    ↓
ApiSearchService
    ├─→ Repository (DB 조회)
    └─→ SchemaSupporter (DB 포맷 → AI 친화적 포맷)
```

## 🛠️ 기술 스택

| 카테고리 | 기술 |
|---------|------|
| Framework | Spring Boot 3.5.9 |
| Language | Java 21 |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA |
| MCP | Spring AI MCP 1.1.2 |
| Parser | Jackson, Swagger Parser 2.1.37 |
| Utility | Lombok |
| Deploy | Docker, GitHub Actions |

## Project Structure

```
src/main/java/com/ndgl/swaggermcp/
├── sync/                          # Swagger 동기화 도메인
│   ├── presentation/http/
│   │   └── SyncController
│   ├── application/
│   │   ├── usecase/
│   │   │   └── SyncSwaggerUseCase  (Facade)
│   │   ├── service/
│   │   │   ├── SwaggerFetchService
│   │   │   ├── SwaggerParserService
│   │   │   ├── SwaggerExtractorService
│   │   │   └── SwaggerSyncService
│   │   └── parser/
│   │       ├── EndpointBasicInfoParser
│   │       ├── RequestSchemaParser
│   │       ├── ResponseSchemaParser
│   │       ├── ErrorResponseParser
│   │       └── TagParser
│   ├── support/
│   │   └── JsonSchemaParsingSupport  ($ref 해석, Example 추출)
│   └── dto/
│       ├── ParsedApiEndpoint
│       ├── ParsedRequestBody
│       ├── ParsedParameter
│       ├── ParsedResponseSchema
│       └── ParsedErrorResponse
│
├── ai/                            # MCP 서버 도메인
│   ├── presentation/mcp/
│   │   └── SwaggerMcpTools        (MCP Tool 구현)
│   ├── service/
│   │   └── ApiSearchService
│   ├── support/
│   │   └── SchemaSupporter        (DB 포맷 → AI 친화적 포맷)
│   └── dto/
│       ├── ApiDetailForAI
│       ├── RequestForAI
│       ├── ResponseForAI
│       ├── ErrorForAI
│       ├── FieldInfo
│       ├── ParameterInfo
│       └── ApiSummary
│
├── persistence/                   # 데이터 접근 계층
│   ├── entity/
│   │   ├── ApiEndpoint
│   │   ├── RequestSchema
│   │   ├── Parameter
│   │   ├── ResponseSchema
│   │   ├── ErrorResponse
│   │   └── SwaggerMetadata
│   └── repository/
│
└── common/
    ├── config/
    │   ├── JpaConfig
    │   └── RestClientConfig
    └── filter/
        └── ApiKeyAuthFilter       (MCP 인증)
```

## MCP Tools Spec

### 1. `searchApiByKeyword(keyword: String)`
**설명**: 키워드로 API 검색
**검색 대상**: path, summary, description, operationId
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
  "request": {
    "body": {
      "email": {"type": "string", "required": true, "example": "user@example.com"},
      "password": {"type": "string", "required": true, "example": "password123"}
    },
    "parameters": []
  },
  "responses": {
    "200": {"fields": {"accessToken": {"type": "string"}, "refreshToken": {"type": "string"}}}
  },
  "errors": {
    "401": {"code": "AUTH-LOGIN-INVALID_CREDENTIALS", "message": "인증 실패"}
  }
}
```

### 3. `getRequestFormat(apiId: Long)`
**설명**: Request Body + Parameters 조회 / **반환**: `RequestForAI`

### 4. `getResponseFormat(apiId: Long)`
**설명**: 상태 코드별 Success Response 조회 / **반환**: `Map<Integer, ResponseForAI>`

### 5. `getErrorFormats(apiId: Long)`
**설명**: 상태 코드별 Error Response 조회 / **반환**: `Map<Integer, ErrorForAI>`
