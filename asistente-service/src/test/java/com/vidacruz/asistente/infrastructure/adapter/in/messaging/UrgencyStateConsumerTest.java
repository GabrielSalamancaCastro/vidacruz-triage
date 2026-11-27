package com.vidacruz.asistente.infrastructure.adapter.in.messaging;

import com.vidacruz.asistente.application.port.out.UrgencyStateRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UrgencyStateConsumerTest {

    private UrgencyStateRepositoryPort repository;
    private UrgencyStateConsumer consumer;

    @BeforeEach
    void setUp() {
        repository = mock(UrgencyStateRepositoryPort.class);
        consumer = new UrgencyStateConsumer(repository);
        when(repository.registrarPaciente(anyString(), anyString())).thenReturn(Mono.empty());
        when(repository.darDeAltaPaciente(anyString(), anyString())).thenReturn(Mono.empty());
    }

    @Test
    void paciente_registrado_incrementa_contador() {
        var evento = Map.of(
                "eventType", "paciente_registrado",
                "pacienteId", "p-001",
                "occurredAt", Instant.now().toString(),
                "payload", Map.of("nivel", "CRITICO")
        );

        consumer.onEvento(evento);

        verify(repository, times(1)).registrarPaciente("p-001", "CRITICO");
    }

    @Test
    void paciente_alta_decrementa_contador() {
        var evento = Map.of(
                "eventType", "paciente_alta",
                "pacienteId", "p-001",
                "occurredAt", Instant.now().toString(),
                "payload", Map.of("nivel", "CRITICO")
        );

        consumer.onEvento(evento);

        verify(repository, times(1)).darDeAltaPaciente("p-001", "CRITICO");
    }

    @Test
    void paciente_escalado_no_modifica_contadores() {
        var evento = Map.of(
                "eventType", "paciente_escalado",
                "pacienteId", "p-001",
                "occurredAt", Instant.now().toString(),
                "payload", Map.of("nivel", "CRITICO")
        );

        consumer.onEvento(evento);

        verify(repository, never()).registrarPaciente(anyString(), anyString());
        verify(repository, never()).darDeAltaPaciente(anyString(), anyString());
    }
}
