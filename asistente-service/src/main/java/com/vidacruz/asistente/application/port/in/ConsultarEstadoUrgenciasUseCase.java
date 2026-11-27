package com.vidacruz.asistente.application.port.in;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface ConsultarEstadoUrgenciasUseCase {

    Mono<Map<String, Long>> consultarEstado();
}
