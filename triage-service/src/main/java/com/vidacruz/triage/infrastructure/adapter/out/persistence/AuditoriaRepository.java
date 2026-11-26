package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AuditoriaRepository extends ReactiveCrudRepository<AuditoriaEntity, String> {
    Flux<AuditoriaEntity> findByPacienteId(String pacienteId);
}
