package com.vidacruz.triage.application.port.out;

import com.vidacruz.triage.domain.model.EventoDominio;
import reactor.core.publisher.Mono;

public interface EventPublisherPort {
    Mono<Void> publicar(EventoDominio evento);
}
