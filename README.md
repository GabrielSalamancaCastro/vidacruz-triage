# VidaCruz — Sistema de Triaje Digital con Asistente Clínico

Sistema backend reactivo para triaje de urgencias con clasificación automática de pacientes,
monitoreo de SLA, escalamiento idempotente y asistente clínico RAG anti-alucinación.

> Prueba Técnica — Dev. WebFlux (Senior / Arquitecto)

---

## 📐 Arquitectura

Diagrama completo en [`docs/diagrams/arquitectura.mmd`](docs/diagrams/arquitectura.mmd).

**Estilo arquitectónico:** Hexagonal pragmático (Ports & Adapters) con 3 capas internas por servicio:

- `domain/` — modelo y reglas de negocio puras (sin Spring, sin Reactor). Testeable en ms.
- `application/` — puertos (`port/in`, `port/out`) y orquestación reactiva (`Mono`/`Flux`).
- `infrastructure/` — adaptadores WebFlux, R2DBC, Kafka, Redis, LLM, seguridad, configuración.

**Microservicios:**

- **`triage-service` (:8081)** — Registro, clasificación RN-01, SLA RN-02, escalamiento RN-03, auditoría RN-04, reportes RF-05, alertas RF-06.
- **`asistente-service` (:8082)** — RAG anti-alucinación RF-04A (RN-05), estado de urgencias RF-04B vía Kafka + Redis.

**Comunicación:** Kafka topic `triaje.eventos` (unidireccional triage → asistente). El triage nunca depende del asistente.

---

## 🧱 Stack

| Capa | Tecnología |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3 + Spring WebFlux |
| Reactividad | Project Reactor (Mono/Flux) |
| Persistencia | PostgreSQL 16 + R2DBC + Flyway |
| Mensajería | Apache Kafka (KRaft) + reactor-kafka |
| Caché / Vista | Redis 7 reactivo |
| IA | Spring AI + pgvector |
| Resiliencia | Resilience4j |
| Observabilidad | Micrometer + Prometheus + Actuator |
| Seguridad | API Key (header `X-API-Key`) |
| Contenedores | Docker / Docker Compose |
| Orquestación | Kubernetes (Azure Container Apps / AKS) |
| Build | Maven (ver justificación abajo) |

---

## 🚀 Cómo correr localmente

### Requisitos

- Java 21
- Docker + Docker Compose
- (Opcional) Maven 3.9+ si quieres compilar fuera de Docker

### Pasos

```bash
# 1. Copiar variables de entorno
cp .env.example .env

# 2. Editar .env y rellenar al menos LLM_API_KEY
#    (el resto tiene valores por defecto válidos para local)

# 3. Levantar todo (Postgres+pgvector, Kafka, Redis, ambos servicios)
docker compose up --build
Servicios disponibles
Servicio	URL
Triage API	http://localhost:8081
Asistente API	http://localhost:8082
Swagger Triage	http://localhost:8081/swagger-ui.html
Swagger Asistente	http://localhost:8082/swagger-ui.html
Health Triage	http://localhost:8081/actuator/health
Health Asistente	http://localhost:8082/actuator/health
Smoke test
bash
bash smoke-test.sh
🔌 APIs
Todos los endpoints requieren header X-API-Key: <API_KEY_HEADER>.

Triage
POST /triage/pacientes — RF-01
bash
curl -X POST http://localhost:8081/triage/pacientes \
  -H "Content-Type: application/json" \
  -H "X-API-Key: vidacruz-dev-api-key" \
  -d '{
    "nombreCompleto": "Juan Pérez",
    "edad": 45,
    "frecuenciaCardiaca": 130,
    "presionSistolica": 180,
    "presionDiastolica": 110,
    "spo2": 97,
    "temperatura": 36.8,
    "dolor": 8,
    "gcs": 15,
    "quejaPrincipal": "Dolor torácico"
  }'
Respuesta 201 Created:

json
{
  "id": "uuid",
  "nivel": "CRITICO",
  "slaMinutos": 5,
  "estado": "REGISTRADO",
  "fechaRegistro": "2026-10-29T15:04:05Z"
}
PATCH /triage/pacientes/{id}/estado — RF-02
bash
curl -X PATCH http://localhost:8081/triage/pacientes/{id}/estado \
  -H "Content-Type: application/json" \
  -H "X-API-Key: vidacruz-dev-api-key" \
  -d '{"estado": "EN_CONSULTA"}'
Transiciones válidas: REGISTRADO → EN_CONSULTA, REGISTRADO → ALTA, EN_CONSULTA → ALTA. Inválida → 409 Conflict.

GET /triage/pacientes/{id} — RF-02
bash
curl http://localhost:8081/triage/pacientes/{id} -H "X-API-Key: vidacruz-dev-api-key"
GET /triage/reportes/operacion?desde=...&hasta=... — RF-05
bash
curl "http://localhost:8081/triage/reportes/operacion?desde=2026-10-29T00:00:00Z&hasta=2026-10-29T23:59:59Z" \
  -H "X-API-Key: vidacruz-dev-api-key"
Asistente
POST /asistente/consulta — RF-04A
bash
curl -X POST http://localhost:8082/asistente/consulta \
  -H "Content-Type: application/json" \
  -H "X-API-Key: vidacruz-dev-api-key" \
  -d '{"pregunta": "¿Cuáles son las medidas iniciales ante una crisis hipertensiva?"}'
Respuesta:

json
{
  "respuesta": "Reconfirmar la presión tras 5 minutos de reposo. No reducir la presión más de un 25% en la primera hora. [PROTO-001]",
  "fuentes": [{"protocolo": "PROTO-001", "seccion": "Medidas iniciales"}]
}
Si no hay protocolo relevante:

json
{
  "respuesta": "No existe un protocolo disponible en el corpus oficial para responder a esta pregunta.",
  "fuentes": []
}
GET /asistente/estado-urgencias — RF-04B
bash
curl http://localhost:8082/asistente/estado-urgencias -H "X-API-Key: vidacruz-dev-api-key"
Respuesta:

json
{"CRITICO": 2, "URGENTE": 3, "URGENTE_2": 0, "NO_URGENTE": 1, "NO_URGENTE_2": 0}
📨 Eventos Kafka — topic triaje.eventos
Evento	Emisor	Consumidor	Descripción
paciente_registrado	Triage	Asistente	Nuevo paciente clasificado
paciente_en_consulta	Triage	Asistente	Paciente entra a consulta
paciente_alta	Triage	Asistente	Paciente dado de alta
paciente_escalado	Triage	Asistente + Alerta webhook	SLA superado
Key: pacienteId (garantiza orden por paciente).

Formato: JSON versionado con schemaVersion, eventType, eventId, occurredAt, pacienteId, payload.

Contrato detallado: docs/events/contratos-eventos.md.

Justificación del formato: ADR-002.

🧭 Decisiones clave (ADRs)
ADR	Tema	Decisión
001	Arquitectura interna	Hexagonal pragmático (3 capas)
002	Formato eventos	JSON versionado en triaje.eventos
003	Vector store RAG	pgvector + Spring AI
004	Fallo LLM	Circuit breaker + 503 explícito
005	RF-06 alertas	Webhook Slack/Discord
Build tool: Maven (decisión consciente, no impuesta)
El enunciado no exige una herramienta de build concreta. Se eligió Maven por tres razones pragmáticas para este contexto:

1. Mucho mas facil leer un pom.xml que un archivo de gradle

2. Estabilidad con Spring AI milestone: spring-ai-bom en versión milestone resuelve limpiamente con un <repository> en el parent POM (Recomendado por DeepSeek)

3. Se considero Gradle ya que se tiene mas experiencia en este pero al ser demasiadas dependencias se cambio a un manejo con maven  

🧪 Tests
bash
# Unitarios (rápidos, sin Docker)
mvn -pl triage-service test
mvn -pl asistente-service test

# Integración (Testcontainers, requiere Docker)
mvn -pl triage-service verify
Cobertura:

RN-01 — Clasificación de E1–E6 del enunciado (24 tests).

RF-02 — Transiciones válidas e inválidas (409).

RN-03 — Idempotencia del escalamiento (doble tick no duplica).

RN-05 — RAG con citas; caso "sin coincidencia" → aviso sin contenido clínico.

Modo degradado — Circuit breaker + fallback 503.

RF-05 — p95 y promedios validados contra cálculo esperado.

RF-06 — Webhook Slack/Discord con MockWebServer.

Los tests de integración (*IT) usan Testcontainers (PostgreSQL, Kafka). Requieren Docker
corriendo y DOCKER_HOST=tcp://localhost:2375 si usas Docker Desktop en Windows (ver
ADR-002).

☁️ Despliegue en Azure
Instrucciones completas en docs/deploy.md.

Resumen:

Local: docker compose up --build

K8s: manifiestos en k8s/ (Deployment, Service, ConfigMap, Secret).

Azure Container Apps: az containerapp up --source . o aplicar los manifiestos.

🤝 Supuestos y ambigüedades resueltas
RF-06 (canal de alertas): abierto por diseño. Se implementaron dos adapters (Slack y Discord) seleccionables por variable de entorno (SLACK_WEBHOOK_URL / DISCORD_WEBHOOK_URL). Si ninguna está configurada, se usa un no-op. Justificación completa en ADR-005.

Timezone: fechas ISO 8601 en UTC.

Escalamiento SLA: UPDATE ... WHERE estado='REGISTRADO' AND escalado=false atómico → idempotencia sin locks distribuidos (RN-03).

Outbox pattern: no implementado (deuda consciente en ADR-002).

Build tool: Maven (justificado arriba).

Vector store: pgvector sobre el mismo Postgres (ADR-003).

RAG real (Fase 9): implementado con Spring AI + pgvector, activable con perfil llm-real. Requiere Spring AI 1.0.0 GA; la versión milestone 1.0.0-M3 usada por el BOM tiene un bug de auto-configuración del VectorStore con R2DBC. El perfil por defecto usa fakes que cumplen el contrato de los puertos.

Autenticación: API Key simple (X-API-Key). Se escogió por temas de practicidad y facilidad. 

Estado de urgencias (RF-04B): Redis con Hash por nivel. Idempotente: si el paciente ya está en el Hash, no se cuenta dos veces. Justificación en ADR-006.

