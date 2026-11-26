package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PacienteRepository extends ReactiveCrudRepository<PacienteEntity, String> {
}
