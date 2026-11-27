package com.vidacruz.asistente.application.port.out;

import com.vidacruz.asistente.domain.model.Fuente;
import reactor.core.publisher.Flux;

public interface VectorStorePort {
    Flux<Fuente> buscarSimilares(String pregunta, int topK);
}
