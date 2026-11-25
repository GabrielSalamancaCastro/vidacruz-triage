package com.vidacruz.triage.domain.service;

import com.vidacruz.triage.domain.model.NivelTriaje;

public class SlaCalculator {
    public int calcularSlaMinutos(NivelTriaje nivel) {
        if (nivel == null) {
            throw new IllegalArgumentException("El nivel de triaje no puede ser nulo");
        }
        return nivel.getSlaMinutos();
    }
}
