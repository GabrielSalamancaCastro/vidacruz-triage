package com.vidacruz.triage.infrastructure.adapter.out.messaging;

import com.vidacruz.triage.application.port.out.EventPublisherPort;
import com.vidacruz.triage.domain.model.EventoDominio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("local-no-kafka")
public class NoOpEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventPublisher.class);

    @Override
    public Mono<Void> publicar(EventoDominio evento) {
        log.info("[NoOpEventPublisher] Evento simulado: type={}, pacienteId={}, payload={}",
                evento.eventType(), evento.pacienteId(), evento.payload());
        return Mono.empty();
    }
}
