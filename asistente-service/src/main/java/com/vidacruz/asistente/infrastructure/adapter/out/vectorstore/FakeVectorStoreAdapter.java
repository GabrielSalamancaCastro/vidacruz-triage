package com.vidacruz.asistente.infrastructure.adapter.out.vectorstore;

import com.vidacruz.asistente.application.port.out.VectorStorePort;
import com.vidacruz.asistente.domain.model.Fuente;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Fake del vector store para Fase 8. Matchea por palabras clave en la pregunta.
 *
 * <p>Activo en el perfil {@code fake-llm} (por defecto en local hasta que
 * se configure el LLM real en Fase 9).
 */
@Component
@Profile("!llm-real")
public class FakeVectorStoreAdapter implements VectorStorePort{

    @Override
    public Flux<Fuente> buscarSimilares(String pregunta, int topK) {
        var p = pregunta.toLowerCase();

        if (p.contains("hipertensiva") || p.contains("presión") || p.contains("presion")) {
            return Flux.just(new Fuente("PROTO-001", "Medidas iniciales"));
        }
        if (p.contains("sepsis") || p.contains("antibiótico") || p.contains("antibiotico")) {
            return Flux.just(new Fuente("PROTO-002", "Acciones iniciales"));
        }
        if (p.contains("deshidratación") || p.contains("deshidratacion") || p.contains("pediátrica")) {
            return Flux.just(new Fuente("PROTO-003", "Rehidratación"));
        }
        return Flux.empty(); // sin coincidencia
    }

}
