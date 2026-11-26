package com.vidacruz.triage.infrastructure.adapter.in.messaging;

import com.vidacruz.triage.application.port.out.AlertPort;
import com.vidacruz.triage.domain.model.AlertaEscalamiento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class EscalamientoAlertConsumer {
    private static final Logger log = LoggerFactory.getLogger(EscalamientoAlertConsumer.class);

    private final AlertPort alertPort;

    public EscalamientoAlertConsumer(AlertPort alertPort) {
        this.alertPort = alertPort;
    }

    @KafkaListener(
            topics = "triaje.eventos",
            groupId = "triage-alertas",
            properties = {
                    "spring.json.value.default.type=java.util.Map",
                    "spring.json.use.type.headers=false"
            }
    )
    public void onEvento(Map<String, Object> evento) {
        var eventType = String.valueOf(evento.get("eventType"));
        if (!"paciente_escalado".equals(eventType)) {
            return; // ignorar otros eventos
        }

        try {
            var payload = (Map<String, Object>) evento.get("payload");
            var alerta = new AlertaEscalamiento(
                    String.valueOf(evento.get("pacienteId")),
                    String.valueOf(payload.get("nivel")),
                    ((Number) payload.get("slaMinutos")).intValue(),
                    ((Number) payload.getOrDefault("tiempoEsperaMinutos", 0)).longValue(),
                    Instant.parse(String.valueOf(evento.get("occurredAt")))
            );

            alertPort.enviarAlertaEscalamiento(alerta)
                    .doOnError(err -> log.warn(
                            "Error enviando alerta de escalamiento para paciente {}: {}",
                            alerta.pacienteId(), err.getMessage()))
                    .onErrorResume(err -> reactor.core.publisher.Mono.empty())
                    .subscribe();
        } catch (Exception e) {
            log.error("Error procesando evento paciente_escalado: {}", evento, e);
        }
    }
}
