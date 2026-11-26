package com.vidacruz.triage.infrastructure.adapter.out.alert;



import com.vidacruz.triage.application.port.out.AlertPort;
import com.vidacruz.triage.domain.model.AlertaEscalamiento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(
        name = {"triage.alerts.slack-webhook-url", "triage.alerts.discord-webhook-url"},
        matchIfMissing = true,
        havingValue = ""
)
public class NoOpAlertAdapter implements AlertPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpAlertAdapter.class);

    @Override
    public Mono<Void> enviarAlertaEscalamiento(AlertaEscalamiento alerta) {
        log.info("[NoOpAlertAdapter] Alerta simulada (sin webhook configurado): {}", alerta);
        return Mono.empty();
    }
}
