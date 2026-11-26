package com.vidacruz.triage.infrastructure.adapter.out.alert;

import com.vidacruz.triage.application.port.out.AlertPort;
import com.vidacruz.triage.domain.model.AlertaEscalamiento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "triage.alerts.slack-webhook-url", matchIfMissing = false)
public class SlackWebhookAdapter implements AlertPort{
    private static final Logger log = LoggerFactory.getLogger(SlackWebhookAdapter.class);

    private final WebClient webClient;
    private final String webhookUrl;

    public SlackWebhookAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${triage.alerts.slack-webhook-url:}") String webhookUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.webhookUrl = webhookUrl;
    }

    @Override
    public Mono<Void> enviarAlertaEscalamiento(AlertaEscalamiento alerta) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.info("[SlackWebhookAdapter] SLACK_WEBHOOK_URL no configurada. Alerta simulada: {}", alerta);
            return Mono.empty();
        }

        var payload = construirPayload(alerta);

        return webClient.post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(r -> log.info("Alerta enviada a Slack: pacienteId={}", alerta.pacienteId()))
                .doOnError(err -> log.error("Error enviando alerta a Slack: pacienteId={}", alerta.pacienteId(), err))
                .then();
    }

    private Map<String, Object> construirPayload(AlertaEscalamiento alerta) {
        return Map.of(
                "text", "🚨 Paciente escalado — nivel " + alerta.nivel(),
                "blocks", List.of(
                        Map.of(
                                "type", "header",
                                "text", Map.of("type", "plain_text", "text", "🚨 Paciente escalado por SLA")
                        ),
                        Map.of(
                                "type", "section",
                                "fields", List.of(
                                        Map.of("type", "mrkdwn", "text", "*Paciente:* `" + alerta.pacienteId() + "`"),
                                        Map.of("type", "mrkdwn", "text", "*Nivel:* " + alerta.nivel()),
                                        Map.of("type", "mrkdwn", "text", "*SLA:* " + alerta.slaMinutos() + " min"),
                                        Map.of("type", "mrkdwn", "text", "*Espera:* " + alerta.tiempoEsperaMinutos() + " min")
                                )
                        )
                )
        );
    }
}
