package com.vidacruz.triage.infrastructure.adapter.out.messaging;

import com.vidacruz.triage.application.port.out.EventPublisherPort;
import com.vidacruz.triage.domain.model.EventoDominio;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Profile("!local-no-kafka")
public class KafkaEventPublisherAdapter implements EventPublisherPort {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherAdapter.class);
    private static final String TOPIC = "triaje.eventos";

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisherAdapter(ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> publicar(EventoDominio evento) {
        var mensaje = Map.of(
                "schemaVersion", "1.0",
                "eventType", evento.eventType(),
                "eventId", java.util.UUID.randomUUID().toString(),
                "occurredAt", evento.occurredAt().toString(),
                "pacienteId", evento.pacienteId(),
                "payload", evento.payload()
        );

        var record = new ProducerRecord<String, Object>(TOPIC, evento.pacienteId(), mensaje);

        return kafkaTemplate.send(record)
                .doOnSuccess(result -> log.info(
                        "Evento publicado en Kafka: type={}, pacienteId={}, partition={}, offset={}",
                        evento.eventType(),
                        evento.pacienteId(),
                        result.recordMetadata().partition(),
                        result.recordMetadata().offset()))
                .doOnError(err -> log.error(
                        "Error publicando evento en Kafka: type={}, pacienteId={}",
                        evento.eventType(), evento.pacienteId(), err))
                .then();
    }
}
