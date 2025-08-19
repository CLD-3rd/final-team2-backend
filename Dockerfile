FROM openjdk:17

# PEM 인증서 복사 (src/main/resources/certs → /app/resources/certs)
COPY src/main/resources/certs /app/certs

# JAR 파일 복사
COPY build/libs/app.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]