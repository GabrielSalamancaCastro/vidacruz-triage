# ADR-004 — Modo degradado ante fallo del LLM

## Contexto

RF-04 y los Requerimientos Técnicos exigen:
- Circuit breaker (Resilience4j) sobre las llamadas al proveedor de LLM.
- Fallback explícito: 503 con mensaje claro cuando el LLM no está disponible.
- El servicio de triaje **nunca** debe depender de la disponibilidad del asistente.

## Decisión

**Circuit breaker + TimeLimiter** sobre `LlmPort`, con fallback que devuelve
`503 Service Unavailable` con un mensaje clínico-seguro.

### Configuración Resilience4j

```java
CircuitBreakerConfig.custom()
        .slidingWindowSize(10)              // ventana de 10 llamadas
        .failureRateThreshold(50)           // si 50% fallan, se abre
        .waitDurationInOpenState(Duration.ofSeconds(30))  // espera 30s
        .permittedNumberOfCallsInHalfOpenState(3)         // 3 llamadas de prueba
        .build()