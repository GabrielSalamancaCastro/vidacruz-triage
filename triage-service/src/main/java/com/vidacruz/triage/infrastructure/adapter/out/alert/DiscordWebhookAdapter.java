package com.vidacruz.triage.infrastructure.adapter.out.alert;

import com.vidacruz.triage.application.port.out.AlertPort;
import com.vidacruz.triage.domain.model.AlertaEscalamiento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;


@Component
@Primary
@ConditionalOnProperty(name = "triage.alerts.discord-webhook-url", matchIfMissing = false)
public class DiscordWebhookAdapter implements AlertPort{

    private static final Logger log = LoggerFactory.getLogger(DiscordWebhookAdapter.class);

    private static final int COLOR_CRITICO = 0xFF0000;

    private final WebClient webClient;
    private final String webhookUrl;

    public DiscordWebhookAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${triage.alerts.discord-webhook-url}") String webhookUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.webhookUrl = webhookUrl;
    }

    @Override
    public Mono<Void> enviarAlertaEscalamiento(AlertaEscalamiento alerta) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.info("[DiscordWebhookAdapter] DISCORD_WEBHOOK_URL no configurada. Alerta simulada: {}", alerta);
            return Mono.empty();
        }

        var payload = construirPayload(alerta);

        return webClient.post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(r -> log.info("Alerta enviada a Discord: pacienteId={}", alerta.pacienteId()))
                .doOnError(err -> log.error("Error enviando alerta a Discord: pacienteId={}", alerta.pacienteId(), err))
                .then();
    }

    private Map<String, Object> construirPayload(AlertaEscalamiento alerta) {
        return Map.of(
                "username", "VidaCruz Triaje",
                "embeds", List.of(Map.of(
                        "title", "🚨 Paciente escalado por SLA",
                        "color", COLOR_CRITICO,
                        "fields", List.of(
                                Map.of("name", "Paciente", "value", "`" + alerta.pacienteId() + "`", "inline", true),
                                Map.of("name", "Nivel", "value", alerta.nivel(), "inline", true),
                                Map.of("name", "SLA", "value", alerta.slaMinutos() + " min", "inline", true),
                                Map.of("name", "Espera", "value", alerta.tiempoEsperaMinutos() + " min", "inline", true)
                        ),
                        "timestamp", alerta.escaladoEn().toString()
                ))
        );
    }
}
