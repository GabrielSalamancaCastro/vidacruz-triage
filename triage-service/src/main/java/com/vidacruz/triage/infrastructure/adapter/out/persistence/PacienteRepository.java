package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PacienteRepository extends ReactiveCrudRepository<PacienteEntity, String> {

    /**
     * Solo permite escalar pacientes SI Y SOLO :
     * 1. No han sido escalados previamente
     * 2. Estan en estado registrados
     * Dos schedulers concurrentes no pueden escalar al mismo paciente dos veces:
     * solo uno de los dos UPDATE afectará 1 fila; el otro afectará 0.
     */
    @Query("""
        UPDATE pacientes 
        SET escalado = true 
        WHERE id = :pacienteId 
          AND escalado = false 
          AND estado = 'REGISTRADO'
        """)
    Mono<Integer> marcarEscaladoSiCorresponde(@Param("pacienteId") String pacienteId);

    Flux<PacienteEntity> findByEstadoAndEscaladoFalse(String estado);
}
