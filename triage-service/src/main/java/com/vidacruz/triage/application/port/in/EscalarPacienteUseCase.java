package com.vidacruz.triage.application.port.in;

import com.vidacruz.triage.domain.model.Paciente;
import reactor.core.publisher.Mono;

public interface EscalarPacienteUseCase {
    Mono<Paciente> escalarSiCorresponde(String pacienteId);
}
