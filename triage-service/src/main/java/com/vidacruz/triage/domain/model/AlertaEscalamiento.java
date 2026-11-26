package com.vidacruz.triage.domain.model;

import java.time.Instant;

public record AlertaEscalamiento(
        String pacienteId,
        String nivel,
        int slaMinutos,
        long tiempoEsperaMinutos,
        Instant escaladoEn
) {
}
