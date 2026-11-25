---

## 6. `docs/events/contratos-eventos.md`

```markdown
# Contratos de eventos — topic `triaje.eventos`

- **Key:** `pacienteId`
- **Value:** JSON envelope (ver [ADR-002](../adr/ADR-002-formato-eventos-kafka.md))
- **Particiones:** 3
- **Retención:** 7 días

## Envelope común

```json
{
  "schemaVersion": "1.0",
  "eventType": "<tipo>",
  "eventId": "<uuid-v4>",
  "occurredAt": "<ISO-8601 UTC>",
  "pacienteId": "<uuid-v4>",
  "payload": { ... }
}