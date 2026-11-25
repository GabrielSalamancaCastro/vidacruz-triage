# VidaCruz — Sistema de Triaje Digital con Asistente Clínico

Sistema backend reactivo para triaje de urgencias con clasificación automática de pacientes,
monitoreo de SLA, escalamiento idempotente y asistente clínico RAG anti-alucinación.

> Prueba Técnica — Dev. WebFlux (Senior / Arquitecto)

---

## 📐 Arquitectura

![Arquitectura VidaCruz](docs/diagrams/arquitectura.png)

> Fuente editable: [`docs/diagrams/arquitectura.mmd`](docs/diagrams/arquitectura.mmd)
> (renderizable en GitHub o en [mermaid.live](https://mermaid.live)).
> Justificación de la arquitectura: [ADR-001](docs/adr/ADR-001-arquitectura-hexagonal.md).

**Estilo arquitectónico:** Hexagonal pragmático (Ports & Adapters) con 3 capas internas por servicio:

- `domain/` — modelo y reglas de negocio puras (sin Spring, sin Reactor). Testeable en ms.
- `application/` — puertos (`port/in`, `port/out`) y orquestación reactiva (`Mono`/`Flux`).
- `infrastructure/` — adaptadores WebFlux, R2DBC, Kafka, Redis, LLM, seguridad, configuración.

> **Nota sobre estructura incremental:** la estructura física de carpetas
> (`domain/`, `application/`, `infrastructure/`) se materializa a medida que cada fase
> del desarrollo las necesita. En el estado actual del repositorio solo existen las clases
> de arranque; las capas se añaden con su primer archivo real, evitando carpetas vacías.
> Esto hace el historial de git autoexplicativo: cada commit muestra qué capa se introdujo
> y por qué.

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
````


### 🔌 APIs (resumen)

| Método | Endpoint | Servicio | RF |
| :--- | :--- | :--- | :--- |
| `POST` | `/triage/pacientes` | Triage | RF-01 |
| `PATCH` | `/triage/pacientes/{id}/estado` | Triage | RF-02 |
| `GET` | `/triage/pacientes/{id}` | Triage | RF-02 |
| `GET` | `/triage/reportes/operacion?desde=&hasta=` | Triage | RF-05 |
| `POST` | `/asistente/consulta` | Asistente | RF-04A |
| `GET` | `/asistente/estado-urgencias` | Asistente | RF-04B |


### 📨 Eventos Kafka — topic triaje.eventos


| Evento | Emisor | Consumidor | Descripción |
| :--- | :--- | :--- | :--- |
| `paciente_registrado` | Triage | Asistente | Nuevo paciente clasificado |
| `paciente_en_consulta` | Triage | Asistente | Paciente entra a consulta |
| `paciente_alta` | Triage | Asistente | Paciente dado de alta |
| `paciente_escalado` | Triage | Asistente + Alerta webhook | SLA superado |

