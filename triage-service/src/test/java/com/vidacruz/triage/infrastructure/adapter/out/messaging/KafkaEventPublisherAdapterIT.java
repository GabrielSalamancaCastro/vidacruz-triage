package com.vidacruz.triage.infrastructure.adapter.out.messaging;

import com.vidacruz.triage.domain.model.EventoDominio;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class KafkaEventPublisherAdapterIT {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaEventPublisherAdapter adapter;

    @Test
    void publica_evento_en_triaje_eventos() {
        var evento = new EventoDominio(
                "paciente_registrado", "p-test-001", Instant.now(),
                Map.of("nivel", "CRITICO", "slaMinutos", 5)
        );

        adapter.publicar(evento).block(Duration.ofSeconds(10));

        var receiverOptions = ReceiverOptions.<String, String>create(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.currentTimeMillis(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        )).subscription(List.of("triaje.eventos"));

        ConsumerRecord<String, String> record = KafkaReceiver.create(receiverOptions)
                .receive().next().block(Duration.ofSeconds(15));

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("p-test-001");
        assertThat(record.value()).contains("paciente_registrado").contains("CRITICO");
    }
}
