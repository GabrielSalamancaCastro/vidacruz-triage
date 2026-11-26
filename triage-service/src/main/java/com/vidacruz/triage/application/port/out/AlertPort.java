package com.vidacruz.triage.application.port.out;

import com.vidacruz.triage.domain.model.AlertaEscalamiento;
import reactor.core.publisher.Mono;

public interface AlertPort {
    Mono<Void> enviarAlertaEscalamiento(AlertaEscalamiento alerta);
}
