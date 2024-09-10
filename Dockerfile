# 1. 베이스 이미지 선택 (OpenJDK 17)
FROM openjdk:17-jdk-slim

# 2. 애플리케이션 JAR 파일을 컨테이너로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3. 포트 설정 (Spring Boot 기본 포트)
EXPOSE 8081

# 4. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]