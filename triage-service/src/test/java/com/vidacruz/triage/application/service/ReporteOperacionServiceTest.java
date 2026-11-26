package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.out.ReporteOperacionRepositoryPort;
import com.vidacruz.triage.domain.exception.DominioException;
import com.vidacruz.triage.domain.model.ReporteHora;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReporteOperacionServiceTest {

    @Test
    void genera_reporte_delegando_al_repositorio() {
        var repo = mock(ReporteOperacionRepositoryPort.class);
        var desde = Instant.parse("2026-10-29T00:00:00Z");
        var hasta = Instant.parse("2026-10-29T23:59:59Z");
        var reporte = new ReporteHora(
                Instant.parse("2026-10-29T10:00:00Z"),
                10L,
                Map.of("CRITICO", 2L, "URGENTE", 3L, "URGENTE_2", 5L,
                        "NO_URGENTE", 0L, "NO_URGENTE_2", 0L),
                12.5,
                45.0
        );
        when(repo.generarReporte(desde, hasta)).thenReturn(Flux.just(reporte));

        var service = new ReporteOperacionService(repo);

        StepVerifier.create(service.generarReporte(desde, hasta))
                .assertNext(r -> {
                    assertThat(r.totalRegistrados()).isEqualTo(10);
                    assertThat(r.porNivel()).containsEntry("CRITICO", 2L);
                    assertThat(r.esperaPromedioMin()).isEqualTo(12.5);
                    assertThat(r.esperaP95Min()).isEqualTo(45.0);
                })
                .verifyComplete();
    }

    @Test
    void fechas_nulas_lanzan_excepcion() {
        var service = new ReporteOperacionService(mock(ReporteOperacionRepositoryPort.class));

        StepVerifier.create(service.generarReporte(null, Instant.now()))
                .expectError(DominioException.class)
                .verify();
    }

    @Test
    void desde_posterior_a_hasta_lanza_excepcion() {
        var service = new ReporteOperacionService(mock(ReporteOperacionRepositoryPort.class));
        var desde = Instant.parse("2026-10-30T00:00:00Z");
        var hasta = Instant.parse("2026-10-29T00:00:00Z");

        StepVerifier.create(service.generarReporte(desde, hasta))
                .expectError(DominioException.class)
                .verify();
    }
}
