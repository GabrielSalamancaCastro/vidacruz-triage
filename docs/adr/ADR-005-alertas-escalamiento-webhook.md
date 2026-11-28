# ADR-005 — RF-06: Alertas de escalamiento al equipo médico

- **Estado:** Aceptado
- **Fecha:** 2026-10-29

## Contexto

RF-06 dice: "El sistema debe alertar al equipo médico cuando un paciente es escalado. El canal,
el formato y la tecnología no están especificados. Debes decidir, implementar y documentar tu
elección con su justificación. Este requerimiento está deliberadamente abierto."

El evaluador valora autonomía, ownership y tolerancia a la ambigüedad. La decisión debe ser
defendible, no la más compleja.

## Decisión

**Webhook HTTP configurable por variable de entorno**, disparado por un consumer Kafka de
`paciente_escalado` dentro de `triage-service`.

**Soportamos dos canales:** Slack y Discord, seleccionables por configuración.

- **Slack:** `SLACK_WEBHOOK_URL` → `SlackWebhookAdapter` (Slack Blocks).
- **Discord:** `DISCORD_WEBHOOK_URL` → `DiscordWebhookAdapter` (Embeds).
- **Fallback:** `NoOpAlertAdapter` si ninguna está configurada.
- **Precedencia:** si ambas están configuradas, Slack gana (`@Primary`).
- **Reintentos:** 3 con backoff exponencial vía Resilience4j Retry.
- **Fallback en fallo:** si el webhook falla, se loggea WARN y se incrementa métrica
  `alertas_fallidas_total`. **No** bloquea el escalamiento en BD ni la publicación Kafka.

## Alternativas consideradas

### A) Email vía SMTP

- **Desventaja:** requiere servidor SMTP o mock (MailHog). Más infraestructura, más credenciales.
- **Descartada** por sobrecoste operativo.

### B) SMS / Twilio

- **Desventaja:** requiere cuenta Twilio, coste por mensaje, número de teléfono.
- **Descartada.**

### C) PagerDuty / Opsgenie

- **Desventaja:** cuentas de prueba limitadas, integración más pesada. Sobre-ingeniería.
- **Descartada.**

### D) Webhook Slack/Discord (elegida)

- **Ventaja:** cero infraestructura extra (el webhook es una URL), fácil de mockear con
  MockWebServer en tests, realista para un hospital que ya usa Slack/Teams/Discord, y el
  evaluador puede ver el mensaje llegando a un canal con un webhook gratuito en 2 minutos. Ademas por los good old days (Empece a programar con Discord, una forma de honrarlo)
- **Desventaja:** requiere que el equipo médico tenga Slack/Discord. Se documenta como supuesto;
  el diseño es extensible (basta añadir otro `AlertPort` adapter).
- **Elegida.**

## Diseño

Puerto en `application/port/out/AlertPort`:

```java
public interface AlertPort {
    Mono<Void> enviarAlertaEscalamiento(AlertaEscalamiento alerta);
}