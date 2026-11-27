package com.vidacruz.asistente.application.service;

import com.vidacruz.asistente.application.port.in.ConsultarEstadoUrgenciasUseCase;
import com.vidacruz.asistente.application.port.out.UrgencyStateRepositoryPort;
import reactor.core.publisher.Mono;

import java.util.Map;

public class ConsultarEstadoUrgenciasService implements ConsultarEstadoUrgenciasUseCase {

    private final UrgencyStateRepositoryPort repository;

    public ConsultarEstadoUrgenciasService(UrgencyStateRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Map<String, Long>> consultarEstado() {
        return repository.obtenerEstado();
    }
}
