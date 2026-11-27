package com.vidacruz.asistente.infrastructure.adapter.out.llm;

import com.vidacruz.asistente.application.port.out.LlmPort;
import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter real del LLM usando Spring AI ChatClient.
 *
 * <p>Activo solo en el perfil {@code llm-real}.
 *
 * <p>El prompt fuerza al LLM a:
 * 1. Responder SOLO con la información de las fuentes.
 * 2. Citar al menos una fuente (RN-05).
 * 3. Si no puede responder, decirlo explícitamente.
 */
@Component
@Profile("llm-real")
public class SpringAiLlmAdapter implements LlmPort{

    private static final Logger log = LoggerFactory.getLogger(SpringAiLlmAdapter.class);

    private final ChatClient chatClient;

    public SpringAiLlmAdapter(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Mono<Respuesta> responder(String pregunta, List<Fuente> contexto) {
        return Mono.defer(() -> generarRespuesta(pregunta, contexto))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Respuesta> generarRespuesta(String pregunta, List<Fuente> contexto) {
        String contextoTexto = contexto.stream()
                .map(f -> "[" + f.protocolo() + " — " + f.seccion() + "]")
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
            Eres un asistente clínico. Responde a la pregunta del médico usando EXCLUSIVAMENTE
            la información de los protocolos proporcionados. NO inventes contenido clínico.
            
            Debes citar al menos una fuente en tu respuesta, usando el formato [PROTO-XXX].
            Si no puedes responder con la información disponible, di exactamente:
            "No existe un protocolo disponible en el corpus oficial para responder a esta pregunta."
            
            Protocolos disponibles:
            """ + contextoTexto;

        String respuestaTexto = chatClient.prompt()
                .system(systemPrompt)
                .user(pregunta)
                .call()
                .content();

        log.info("Respuesta del LLM generada: {}", respuestaTexto);

        List<Fuente> fuentesCitadas = contexto.stream()
                .filter(f -> respuestaTexto.contains("[" + f.protocolo() + "]"))
                .toList();

        if (fuentesCitadas.isEmpty()) {
            return Mono.just(new Respuesta(
                    "No existe un protocolo disponible en el corpus oficial para responder a esta pregunta.",
                    List.of()
            ));
        }

        return Mono.just(new Respuesta(respuestaTexto, fuentesCitadas));
}
}
