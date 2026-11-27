FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY triage-service/pom.xml triage-service/pom.xml
COPY asistente-service/pom.xml asistente-service/pom.xml

RUN mvn -B -pl asistente-service dependency:go-offline -DskipTests || true

COPY asistente-service/src asistente-service/src

RUN mvn -B -pl asistente-service clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/asistente-service/target/*.jar app.jar

EXPOSE 8082

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]