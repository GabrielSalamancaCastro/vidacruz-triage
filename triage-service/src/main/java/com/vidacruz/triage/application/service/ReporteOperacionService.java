package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.in.ReporteOperacionUseCase;
import com.vidacruz.triage.application.port.out.ReporteOperacionRepositoryPort;
import com.vidacruz.triage.domain.exception.DominioException;
import com.vidacruz.triage.domain.model.ReporteHora;
import reactor.core.publisher.Flux;

import java.time.Instant;

public class ReporteOperacionService implements ReporteOperacionUseCase {
    private final ReporteOperacionRepositoryPort repository;

    public ReporteOperacionService(ReporteOperacionRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Flux<ReporteHora> generarReporte(Instant desde, Instant hasta) {
        if (desde == null || hasta == null) {
            return Flux.error(new DominioException("fechas",
                    "Los parámetros 'desde' y 'hasta' son obligatorios (ISO 8601)"));
        }
        if (desde.isAfter(hasta)) {
            return Flux.error(new DominioException("fechas",
                    "El parámetro 'desde' no puede ser posterior a 'hasta'"));
        }
        return repository.generarReporte(desde, hasta);
    }
}
