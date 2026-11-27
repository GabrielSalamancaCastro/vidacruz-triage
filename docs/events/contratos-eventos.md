# Contratos de eventos — topic `triaje.eventos`

## Configuración del topic

- **Topic:** `triaje.eventos`
- **Key:** `pacienteId`
- **Value:** JSON envelope
- **Particiones:** 3
- **Retención:** 7 días
- **Productor:** `triage-service`
- **Consumidor:** `asistente-service`

## Envelope común

Todos los eventos utilizan el siguiente formato:

```json
{
  "schemaVersion": "1.0",
  "eventType": "paciente_registrado",
  "eventId": "uuid-v4",
  "occurredAt": "2026-11-27T10:00:00Z",
  "pacienteId": "uuid-v4",
  "payload": {}
}
```

### Campos comunes

| Campo | Descripción |
|---|---|
| schemaVersion | Versión del contrato |
| eventType | Tipo de evento |
| eventId | Identificador único del evento |
| occurredAt | Fecha de generación del evento UTC |
| pacienteId | Identificador del paciente |
| payload | Datos específicos del evento |

## 1. paciente_registrado

```json
{
  "nivel": "CRITICO",
  "estado": "REGISTRADO",
  "escalado": false,
  "slaMinutos": 5,
  "fechaRegistro": "2026-11-27T10:00:00Z"
}
```

## 2. paciente_en_consulta

```json
{
  "nivel": "CRITICO",
  "estado": "EN_CONSULTA",
  "escalado": false
}
```

El campo escalado conserva el valor real del paciente.

## 3. paciente_escalado

```json
{
  "nivel": "CRITICO",
  "estado": "REGISTRADO",
  "escalado": true,
  "slaMinutos": 5,
  "escaladoEn": "2026-11-27T10:06:00Z"
}
```

## 4. paciente_alta

```json
{
  "nivel": "CRITICO",
  "estado": "ALTA",
  "escalado": false
}
```

## Reglas de consumo

1. Los eventos de un mismo paciente conservan su orden relativo mediante la key `pacienteId`.
2. El consumidor debe ser idempotente utilizando `eventId`.
3. Los pacientes en estado ALTA no forman parte del estado de urgencias activo.
4. Los pacientes EN_CONSULTA sí forman parte del estado de urgencias activo.
5. Un paciente debe pertenecer como máximo a un conjunto de nivel.
6. El flag de escalamiento no modifica el nivel de triaje.
7. Los eventos desconocidos o con versiones no soportadas deben manejarse explícitamente y no modificar el estado.

## Proyección en Redis

- `urgencias:{nivel}`: conjunto de IDs de pacientes activos por nivel.
- `paciente:{pacienteId}`: Hash con el estado actual del paciente.
- `urgencias:processed-events`: registro de eventos procesados para idempotencia.