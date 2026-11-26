package com.vidacruz.triage.application.port.out;

import com.vidacruz.triage.domain.model.ReporteHora;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface ReporteOperacionRepositoryPort {
    Flux<ReporteHora> generarReporte(Instant desde, Instant hasta);
}
