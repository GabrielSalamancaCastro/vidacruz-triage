package com.vidacruz.triage.infrastructure.adapter.in.web.dto;

import com.vidacruz.triage.domain.model.Paciente;

import java.time.Duration;
import java.time.Instant;

public record PacienteDetalleResponse(
        String id,
        String nombreCompleto,
        int edad,
        String quejaPrincipal,
        String nivel,
        int slaMinutos,
        String estado,
        boolean escalado,
        Instant fechaRegistro,
        long tiempoEnEstadoActualMinutos
) {
    public static PacienteDetalleResponse desde(Paciente p, Instant ultimoCambioEstado) {
        return new PacienteDetalleResponse(
                p.id(),
                p.nombreCompleto(),
                p.edad(),
                p.quejaPrincipal(),
                p.nivelTriaje().getNombre(),
                p.slaMinutos(),
                p.estado().name(),
                p.escalado(),
                p.fechaRegistro(),
                Duration.between(ultimoCambioEstado, Instant.now()).toMinutes()
        );
    }
}