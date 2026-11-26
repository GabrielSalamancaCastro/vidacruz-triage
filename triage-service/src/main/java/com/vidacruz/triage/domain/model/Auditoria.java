package com.vidacruz.triage.domain.model;

import java.time.Instant;

public record Auditoria(
        String id,
        String pacienteId,
        String estadoAnterior,
        String estadoNuevo,
        String tipoEvento,
        Instant timestamp
) {
}
