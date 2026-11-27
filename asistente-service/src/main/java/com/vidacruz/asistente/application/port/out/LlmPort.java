package com.vidacruz.asistente.application.port.out;

import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LlmPort {
    Mono<Respuesta> responder(String pregunta, List<Fuente> contexto);
}
