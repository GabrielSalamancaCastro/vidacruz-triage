# ADR-001 — Arquitectura interna: Hexagonal pragmático

## Contexto

El sistema tiene dos servicios con lógica de negocio densa y varias integraciones externas
(PostgreSQL reactivo, Kafka, Redis, LLM, vector store). La RN-01 (clasificación), RN-02 (SLA),
la máquina de estados de RF-02 y la idempotencia de RN-03 son reglas puras que deben testearse
sin infraestructura. A su vez, RF-04 exige resiliencia y modo degradado del LLM, lo que
implica poder mockear el proveedor de IA limpiamente.


## Decisión

Usar **arquitectura hexagonal pragmática** con tres capas internas por servicio:

- `domain/` — modelo, servicios de dominio puros (sin Spring, sin Reactor). Testeable en ms.
- `application/` — puertos (`port/in`, `port/out`) y servicios de orquestación reactiva (`Mono`/`Flux`).
- `infrastructure/` — adaptadores WebFlux, R2DBC, Kafka, Redis, LLM, seguridad, configuración.

Las dependencias apuntan **hacia `domain`**. `domain` no importa nada de Spring ni de Reactor.
`application` declara interfaces (puertos) y orquesta. `infrastructure` implementa esas interfaces
y traduce entre el mundo exterior y el dominio.

## Estructura por servicio:
````
com.vidacruz.<servicio>/
├── domain/
│ ├── model/ # Entidades, value objects, enums
│ ├── service/ # Lógica de negocio pura (RN-01, RN-02, RN-03, RN-05)
│ └── exception/ # Excepciones de dominio
├── application/
│ ├── port/in/ # Interfaces de casos de uso
│ ├── port/out/ # Interfaces de repositorios, publishers, LLM, etc.
│ └── service/ # Implementación de casos de uso (reactiva)
└── infrastructure/
├── adapter/in/web/ # Controllers WebFlux
├── adapter/out/persistence/# R2DBC
├── adapter/out/messaging/ # Kafka
├── adapter/out/llm/ # Spring AI
└── config/ # Beans, seguridad, observabilidad