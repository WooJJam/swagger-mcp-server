# 멀티 스테이지 빌드 - 빌드 스테이지
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# 의존성 캐시를 위해 gradle 설정 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (레이어 캐시 활용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle clean build -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-XX:+UseContainerSupport", "-jar", "app.jar"]
