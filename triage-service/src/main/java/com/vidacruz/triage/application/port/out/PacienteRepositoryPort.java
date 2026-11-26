package com.vidacruz.triage.application.port.out;

import com.vidacruz.triage.domain.model.Paciente;
import reactor.core.publisher.Mono;

public interface PacienteRepositoryPort {
    Mono<Paciente> guardar(Paciente paciente);
    Mono<Paciente> buscarPorId(String id);
}
