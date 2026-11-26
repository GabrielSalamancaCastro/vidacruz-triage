package com.vidacruz.triage.application.port.in;

import com.vidacruz.triage.domain.model.Paciente;
import com.vidacruz.triage.domain.model.SignosVitales;
import reactor.core.publisher.Mono;

public interface RegistrarPacienteUseCase {
    Mono<Paciente> registrar(
            String nombreCompleto,
            int edad,
            SignosVitales signosVitales,
            String quejaPrincipal
    );
}
