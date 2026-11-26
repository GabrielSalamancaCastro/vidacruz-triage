package com.vidacruz.triage.domain.model;

import java.time.Instant;
import java.util.Map;

public record EventoDominio(
        String eventType,
        String pacienteId,
        Instant occurredAt,
        Map<String, Object> payload
) {
    public EventoDominio {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType es obligatorio");
        }
        if (pacienteId == null || pacienteId.isBlank()) {
            throw new IllegalArgumentException("pacienteId es obligatorio");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt es obligatorio");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload es obligatorio");
        }
    }
}
