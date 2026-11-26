package com.vidacruz.triage.application.port.in;

import com.vidacruz.triage.domain.model.Paciente;
import reactor.core.publisher.Mono;

public interface ObtenerPacienteUseCase {
    Mono<Paciente> obtener(String pacienteId);
}
