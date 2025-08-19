FROM openjdk:17

# 작업 디렉토리 설정
WORKDIR /app

# PEM 인증서를 시스템 CA 위치에 복사 (확장자 꼭 .crt여야 함)
COPY src/main/resources/certs/rds-combined-ca-bundle.pem /usr/local/share/ca-certificates/rds-combined-ca-bundle.crt

# 시스템 CA 목록 갱신 (PEM → 시스템 신뢰 CA로 등록)
RUN apt-get update && apt-get install -y ca-certificates && update-ca-certificates

# JAR 복사
COPY build/libs/app.jar app.jar

# 앱 실행
ENTRYPOINT ["java", "-jar", "app.jar"]