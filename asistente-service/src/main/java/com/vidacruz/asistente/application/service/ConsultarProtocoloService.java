package com.vidacruz.asistente.application.service;

import com.vidacruz.asistente.application.port.in.ConsultarProtocoloUseCase;
import com.vidacruz.asistente.application.port.out.LlmPort;
import com.vidacruz.asistente.application.port.out.VectorStorePort;
import com.vidacruz.asistente.domain.model.Respuesta;
import reactor.core.publisher.Mono;

public class ConsultarProtocoloService implements ConsultarProtocoloUseCase {
    private static final int TOP_K = 3;

    private final VectorStorePort vectorStore;
    private final LlmPort llm;

    public ConsultarProtocoloService(VectorStorePort vectorStore, LlmPort llm) {
        this.vectorStore = vectorStore;
        this.llm = llm;
    }

    @Override
    public Mono<Respuesta> consultar(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) {
            return Mono.just(Respuesta.sinProtocolo());
        }

        return vectorStore.buscarSimilares(pregunta, TOP_K)
                .collectList()
                .flatMap(fuentes -> {
                    if (fuentes.isEmpty()) {
                        // RN-05: sin fuentes, sin contenido clínico
                        return Mono.just(Respuesta.sinProtocolo());
                    }
                    return llm.responder(pregunta, fuentes)
                            .map(respuesta -> validarCitas(respuesta, fuentes));
                });
    }

    private Respuesta validarCitas(Respuesta respuesta, java.util.List<com.vidacruz.asistente.domain.model.Fuente> fuentes) {
        if (respuesta.tieneFuentes()) {
            return respuesta;
        }
        // El LLM no citó fuentes → respuesta inválida → aviso
        return Respuesta.sinProtocolo();
    }
}
