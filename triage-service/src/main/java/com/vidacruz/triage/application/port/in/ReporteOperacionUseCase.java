package com.vidacruz.triage.application.port.in;

import com.vidacruz.triage.domain.model.ReporteHora;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface ReporteOperacionUseCase {
    Flux<ReporteHora> generarReporte(Instant desde, Instant hasta);
}
