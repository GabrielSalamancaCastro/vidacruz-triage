package com.vidacruz.asistente.infrastructure.adapter.out.llm;

import com.vidacruz.asistente.application.port.out.LlmPort;
import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Fake del LLM para Fase 8. Devuelve respuestas hardcodeadas con la primera fuente.
 *
 * <p>Activo en el perfil {@code !llm-real}.
 */
@Component
@Profile("!llm-real")
public class FakeLlmAdapter implements LlmPort{
    @Override
    public Mono<Respuesta> responder(String pregunta, List<Fuente> contexto) {
        if (contexto.isEmpty()) {
            return Mono.just(Respuesta.sinProtocolo());
        }

        var fuente = contexto.get(0);
        var respuesta = "[FAKE] Respuesta simulada basada en " + fuente.protocolo() +
                " (" + fuente.seccion() + ") para: " + pregunta;

        return Mono.just(new Respuesta(respuesta, List.of(fuente)));
    }
}
