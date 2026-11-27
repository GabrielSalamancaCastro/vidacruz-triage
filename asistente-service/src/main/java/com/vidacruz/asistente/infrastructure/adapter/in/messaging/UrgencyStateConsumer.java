package com.vidacruz.asistente.infrastructure.adapter.in.messaging;

import com.vidacruz.asistente.application.port.out.UrgencyStateRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UrgencyStateConsumer {
    private static final Logger log = LoggerFactory.getLogger(UrgencyStateConsumer.class);

    private final UrgencyStateRepositoryPort repository;

    public UrgencyStateConsumer(UrgencyStateRepositoryPort repository) {
        this.repository = repository;
    }

    @KafkaListener(
            topics = "triaje.eventos",
            groupId = "asistente-urgencias",
            properties = {
                    "spring.json.value.default.type=java.util.Map",
                    "spring.json.use.type.headers=false"
            }
    )
    public void onEvento(Map<String, Object> evento) {
        try {
            var eventType = String.valueOf(evento.get("eventType"));
            var pacienteId = String.valueOf(evento.get("pacienteId"));
            var payload = (Map<String, Object>) evento.get("payload");

            switch (eventType) {
                case "paciente_registrado" -> {
                    var nivel = String.valueOf(payload.get("nivel"));
                    repository.registrarPaciente(pacienteId, nivel)
                            .doOnSuccess(v -> log.info(
                                    "Estado actualizado: paciente {} registrado en nivel {}",
                                    pacienteId, nivel))
                            .doOnError(err -> log.error(
                                    "Error actualizando Redis para paciente {}", pacienteId, err))
                            .subscribe();
                }
                case "paciente_alta" -> {
                    var nivel = String.valueOf(payload.get("nivel"));
                    repository.darDeAltaPaciente(pacienteId, nivel)
                            .doOnSuccess(v -> log.info(
                                    "Estado actualizado: paciente {} dado de alta del nivel {}",
                                    pacienteId, nivel))
                            .doOnError(err -> log.error(
                                    "Error actualizando Redis para paciente {}", pacienteId, err))
                            .subscribe();
                }
                case "paciente_escalado", "paciente_en_consulta" -> {
                    // No cambian contadores de nivel (el paciente sigue activo)
                    log.debug("Evento {} no modifica contadores de nivel", eventType);
                }
                default -> log.warn("Evento desconocido: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error procesando evento: {}", evento, e);
        }
    }
}
