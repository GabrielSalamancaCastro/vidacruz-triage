package com.vidacruz.triage.infrastructure.adapter.in.web.dto;

import com.vidacruz.triage.domain.model.Paciente;

import java.time.Instant;

public record PacienteResponse(
        String id,
        String nivel,
        int slaMinutos,
        String estado,
        Instant fechaRegistro
) {
    public static PacienteResponse desde(Paciente paciente) {
        return new PacienteResponse(
                paciente.id(),
                paciente.nivelTriaje().getNombre(),
                paciente.slaMinutos(),
                paciente.estado().name(),
                paciente.fechaRegistro()
        );
    }
}
