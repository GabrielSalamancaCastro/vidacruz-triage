package com.vidacruz.triage.infrastructure.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@Import(ApiKeyFilter.class)
@TestPropertySource(properties = {
        "triage.security.enabled=true",
        "triage.security.api-key=test-key"
})
@Disabled
public class ApiKeyFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void sin_api_key_devuelve_401() {
        webTestClient.post()
                .uri("/triage/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void con_api_key_incorrecta_devuelve_401() {
        webTestClient.post()
                .uri("/triage/pacientes")
                .header("X-API-Key", "otra-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void actuator_no_requiere_api_key() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
