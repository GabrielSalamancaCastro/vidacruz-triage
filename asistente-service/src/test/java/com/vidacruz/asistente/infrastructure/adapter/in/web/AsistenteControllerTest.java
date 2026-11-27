package com.vidacruz.asistente.infrastructure.adapter.in.web;

import com.vidacruz.asistente.application.port.in.ConsultarProtocoloUseCase;
import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = AsistenteController.class)
public class AsistenteControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ConsultarProtocoloUseCase useCase;

    @Test
    void consulta_con_fuentes_devuelve_200() {
        when(useCase.consultar(anyString())).thenReturn(Mono.just(
                new Respuesta("Reconfirmar la presión.", List.of(new Fuente("PROTO-001", "Medidas iniciales")))
        ));

        webTestClient.post()
                .uri("/asistente/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"pregunta\": \"¿Medidas iniciales en crisis hipertensiva?\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.respuesta").isEqualTo("Reconfirmar la presión.")
                .jsonPath("$.fuentes[0].protocolo").isEqualTo("PROTO-001");
    }

    @Test
    void sin_protocolo_devuelve_aviso_sin_fuentes() {
        when(useCase.consultar(anyString())).thenReturn(Mono.just(Respuesta.sinProtocolo()));

        webTestClient.post()
                .uri("/asistente/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"pregunta\": \"¿Diabetes tipo 2?\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.respuesta").isEqualTo(Respuesta.MENSAJE_SIN_PROTOCOLO)
                .jsonPath("$.fuentes").isEmpty();
    }

    @Test
    void pregunta_vacia_devuelve_400() {
        webTestClient.post()
                .uri("/asistente/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"pregunta\": \"\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
