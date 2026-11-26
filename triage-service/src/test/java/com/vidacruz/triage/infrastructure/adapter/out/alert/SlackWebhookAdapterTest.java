package com.vidacruz.triage.infrastructure.adapter.out.alert;

import com.vidacruz.triage.domain.model.AlertaEscalamiento;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class SlackWebhookAdapterTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void envia_alerta_y_recibe_200() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

        var adapter = new SlackWebhookAdapter(
                WebClient.builder(),
                mockWebServer.url("/webhook").toString()
        );

        var alerta = new AlertaEscalamiento(
                "p-001", "CRITICO", 5, 7L, Instant.now()
        );

        StepVerifier.create(adapter.enviarAlertaEscalamiento(alerta))
                .verifyComplete();

        var request = mockWebServer.getRequestCount();
        assertThat(request).isEqualTo(1);
    }

    @Test
    void sin_webhook_configurado_es_no_op() {
        var adapter = new SlackWebhookAdapter(WebClient.builder(), "");

        var alerta = new AlertaEscalamiento(
                "p-001", "CRITICO", 5, 7L, Instant.now()
        );

        StepVerifier.create(adapter.enviarAlertaEscalamiento(alerta))
                .verifyComplete();

        // No se hizo request al mockWebServer
        assertThat(mockWebServer.getRequestCount()).isZero();
    }
}
