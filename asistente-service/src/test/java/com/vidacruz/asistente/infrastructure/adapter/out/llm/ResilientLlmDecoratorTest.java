package com.vidacruz.asistente.infrastructure.adapter.out.llm;

import com.vidacruz.asistente.application.port.out.LlmPort;
import com.vidacruz.asistente.domain.exception.LlmNoDisponibleException;
import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

public class ResilientLlmDecoratorTest {

    @Test
    void si_llm_falla_repetidamente_circuito_se_abre_y_devuelve_error() {
        var delegate = mock(LlmPort.class);
        when(delegate.responder(anyString(), any()))
                .thenReturn(Mono.error(new RuntimeException("LLM caído")));

        var registry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build());

        var decorator = new ResilientLlmDecorator(delegate, registry);
        var fuente = new Fuente("PROTO-001", "Medidas iniciales");

        // 2 llamadas fallidas → circuit breaker se abre
        StepVerifier.create(decorator.responder("pregunta", List.of(fuente)))
                .expectError(LlmNoDisponibleException.class)
                .verify();
        StepVerifier.create(decorator.responder("pregunta", List.of(fuente)))
                .expectError(LlmNoDisponibleException.class)
                .verify();

        // 3ª llamada → circuito abierto → error inmediato
        StepVerifier.create(decorator.responder("pregunta", List.of(fuente)))
                .expectError(LlmNoDisponibleException.class)
                .verify();
    }

    @Test
    void si_llm_funciona_devuelve_respuesta() {
        var delegate = mock(LlmPort.class);
        var fuente = new Fuente("PROTO-001", "Medidas iniciales");
        var respuesta = new Respuesta("Contenido [PROTO-001]", List.of(fuente));
        when(delegate.responder(anyString(), any())).thenReturn(Mono.just(respuesta));

        var registry = CircuitBreakerRegistry.ofDefaults();
        var decorator = new ResilientLlmDecorator(delegate, registry);

        StepVerifier.create(decorator.responder("pregunta", List.of(fuente)))
                .assertNext(r -> {
                    assertThat(r.respuesta()).contains("PROTO-001");
                })
                .verifyComplete();
    }
}
