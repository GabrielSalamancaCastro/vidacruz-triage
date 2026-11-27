package com.vidacruz.asistente.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class AsistenteMetrics {

    private final MeterRegistry registry;

    public AsistenteMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public Timer timerLlm() {
        return Timer.builder("llm.latency").register(registry);
    }

    public void incrementarErrorLlm() {
        Counter.builder("llm.errores").register(registry).increment();
    }
}
