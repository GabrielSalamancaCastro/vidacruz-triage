package com.vidacruz.triage.domain.model;

import com.vidacruz.triage.domain.exception.DominioException;

import java.time.Instant;

public record Paciente(
        String id,
        String nombreCompleto,
        int edad,
        SignosVitales signosVitales,
        String quejaPrincipal,
        NivelTriaje nivelTriaje,
        EstadoPaciente estado,
        boolean escalado,
        Instant fechaRegistro
) {
    public Paciente {

        if (nombreCompleto == null || nombreCompleto.trim().length() < 3
                || nombreCompleto.trim().length() > 100) {
            throw new DominioException("nombreCompleto",
                    "El nombre completo debe tener entre 3 y 100 caracteres");
        }
        if (edad < 0 || edad > 120) {
            throw new DominioException("edad",
                    "La edad debe estar entre 0 y 120 años");
        }
        if (quejaPrincipal == null || quejaPrincipal.isBlank()) {
            throw new DominioException("quejaPrincipal",
                    "La queja principal no puede estar vacía");
        }
        if (signosVitales == null) {
            throw new DominioException("signosVitales",
                    "Los signos vitales son obligatorios");
        }
        if (nivelTriaje == null) {
            throw new DominioException("nivelTriaje",
                    "El nivel de triaje es obligatorio");
        }
        if (estado == null) {
            throw new DominioException("estado",
                    "El estado es obligatorio");
        }
        if (fechaRegistro == null) {
            throw new DominioException("fechaRegistro",
                    "La fecha de registro es obligatoria");
        }
    }

    public int slaMinutos() {
        return nivelTriaje.getSlaMinutos();
    }

    public boolean estaActivo() {
        return estado != EstadoPaciente.ALTA;
    }
}
