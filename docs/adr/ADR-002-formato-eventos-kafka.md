# ADR-002 — Formato de eventos Kafka

## Contexto

El sistema publica 4 eventos en el topic `triaje.eventos`:
`paciente_registrado`, `paciente_en_consulta`, `paciente_alta`, `paciente_escalado`.
El productor es `triage-service`; los consumidores son `asistente-service` (para reconstruir
el estado de urgencias en Redis) y un listener de alertas RF-06.

El formato del mensaje es libre según el enunciado, pero debe documentarse.

## Decisión

**JSON plano versionado**, con key = `pacienteId` y value = envelope con campos comunes:

```json
{
  "schemaVersion": "1.0",
  "eventType": "paciente_registrado",
  "eventId": "uuid-v4",
  "occurredAt": "2026-10-29T15:04:05.123Z",
  "pacienteId": "uuid-v4",
  "payload": { ... }
}