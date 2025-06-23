# 1단계: 빌드 단계
FROM openjdk:21-jdk-slim as build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper와 빌드 스크립트 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 실행 권한 부여 및 애플리케이션 빌드
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# 2단계: 실행 단계
FROM openjdk:21-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"] 