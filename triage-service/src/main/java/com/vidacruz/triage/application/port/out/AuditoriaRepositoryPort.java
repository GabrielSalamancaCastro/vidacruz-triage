package com.vidacruz.triage.application.port.out;

import com.vidacruz.triage.domain.model.Auditoria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuditoriaRepositoryPort {
    Mono<Auditoria> guardar(Auditoria auditoria);
    Flux<Auditoria> buscarPorPaciente(String pacienteId);
}
