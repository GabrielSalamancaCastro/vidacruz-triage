# ============================================================
# Stage 1: build con Maven (multi-módulo)
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar el parent pom y los poms de módulos para cachear dependencias
COPY pom.xml .
COPY triage-service/pom.xml triage-service/pom.xml
COPY asistente-service/pom.xml asistente-service/pom.xml

# Descargar dependencias del módulo de triage
RUN mvn -B -pl triage-service dependency:go-offline -DskipTests || true

# Copiar el código fuente del módulo
COPY triage-service/src triage-service/src

# Compilar solo el módulo de triage
RUN mvn -B -pl triage-service clean package -DskipTests

# ============================================================
# Stage 2: runtime con JRE slim
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/triage-service/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]