package com.vidacruz.asistente.application.service;

import com.vidacruz.asistente.application.port.out.UrgencyStateRepositoryPort;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsultarEstadoUrgenciasServiceTest {

    @Test
    void devuelve_estado_del_repositorio() {
        var repo = mock(UrgencyStateRepositoryPort.class);
        var estado = Map.of("CRITICO", 2L, "URGENTE", 3L, "URGENTE_2", 0L,
                "NO_URGENTE", 1L, "NO_URGENTE_2", 0L);
        when(repo.obtenerEstado()).thenReturn(Mono.just(estado));

        var service = new ConsultarEstadoUrgenciasService(repo);

        StepVerifier.create(service.consultarEstado())
                .assertNext(r -> {
                    assertThat(r).containsEntry("CRITICO", 2L);
                    assertThat(r).containsEntry("URGENTE", 3L);
                })
                .verifyComplete();
    }
}
