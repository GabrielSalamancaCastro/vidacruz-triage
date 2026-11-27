package com.vidacruz.asistente.application.port.in;

import com.vidacruz.asistente.domain.model.Respuesta;
import reactor.core.publisher.Mono;

public interface ConsultarProtocoloUseCase {
    Mono<Respuesta> consultar(String pregunta);
}
