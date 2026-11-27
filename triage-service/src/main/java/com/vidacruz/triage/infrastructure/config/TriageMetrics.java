package com.vidacruz.triage.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TriageMetrics {

    private final MeterRegistry registry;

    public TriageMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void incrementarClasificacion(String nivel) {
        Counter.builder("triage.clasificacion.nivel")
                .tag("nivel", nivel)
                .register(registry)
                .increment();
    }

    public void incrementarEscalamiento() {
        Counter.builder("triage.escalamientos")
                .register(registry)
                .increment();
    }
}
