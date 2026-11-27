package com.vidacruz.asistente.infrastructure.adapter.out.llm;

import com.vidacruz.asistente.application.port.out.LlmPort;
import com.vidacruz.asistente.domain.exception.LlmNoDisponibleException;
import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Primary
@Profile("llm-real")
public class ResilientLlmDecorator implements LlmPort{

    private static final Logger log = LoggerFactory.getLogger(ResilientLlmDecorator.class);

    private final LlmPort delegate;
    private final CircuitBreaker circuitBreaker;

    public ResilientLlmDecorator(
            LlmPort delegate,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("llm");
    }

    @Override
    public Mono<Respuesta> responder(String pregunta, List<Fuente> contexto) {
        return delegate.responder(pregunta, contexto)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(ex -> {
                    if (ex instanceof LlmNoDisponibleException) {
                        return Mono.error(ex);
                    }
                    log.warn("Error del LLM, activando fallback: {}", ex.getMessage());
                    return Mono.error(new LlmNoDisponibleException(
                            "El asistente clínico no está disponible temporalmente. " +
                                    "Consulte los protocolos directamente o intente de nuevo en unos minutos.",
                            ex));
                });
    }
}
