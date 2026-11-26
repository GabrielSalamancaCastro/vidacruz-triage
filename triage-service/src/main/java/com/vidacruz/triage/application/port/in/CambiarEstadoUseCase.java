package com.vidacruz.triage.application.port.in;

import com.vidacruz.triage.domain.model.Paciente;
import reactor.core.publisher.Mono;

public interface CambiarEstadoUseCase {
    Mono<Paciente> cambiarEstado(String pacienteId, String nuevoEstado);
}
