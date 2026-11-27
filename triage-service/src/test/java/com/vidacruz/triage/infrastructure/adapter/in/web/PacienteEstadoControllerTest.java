package com.vidacruz.triage.infrastructure.adapter.in.web;

import com.vidacruz.triage.application.port.in.CambiarEstadoUseCase;
import com.vidacruz.triage.application.port.in.ObtenerPacienteUseCase;
import com.vidacruz.triage.domain.exception.DominioException;
import com.vidacruz.triage.domain.exception.TransicionInvalidaException;
import com.vidacruz.triage.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PacienteEstadoController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "triage.security.enabled=false") // -> Se añade solamente para que los test evaluen el comportamiento de los endpoints sin API KEY
public class PacienteEstadoControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CambiarEstadoUseCase cambiarEstadoUseCase;

    @MockBean
    private ObtenerPacienteUseCase obtenerPacienteUseCase;

    private static final Paciente PACIENTE = new Paciente(
            "p-001", "Juan Pérez", 45,
            new SignosVitales(130, 180, 110, 97, 36.8, 8, 15),
            "Dolor torácico", NivelTriaje.CRITICO,
            EstadoPaciente.EN_CONSULTA, false, Instant.parse("2026-10-29T15:00:00Z")
    );

    @Test
    void patch_estado_devuelve_200() {
        when(cambiarEstadoUseCase.cambiarEstado(anyString(), anyString())).thenReturn(Mono.just(PACIENTE));

        webTestClient.patch()
                .uri("/triage/pacientes/p-001/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"estado\": \"EN_CONSULTA\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("p-001")
                .jsonPath("$.estado").isEqualTo("EN_CONSULTA");
    }

    @Test
    void patch_estado_invalido_devuelve_409() {
        when(cambiarEstadoUseCase.cambiarEstado(anyString(), anyString()))
                .thenReturn(Mono.error(new TransicionInvalidaException("ALTA → EN_CONSULTA inválida")));

        webTestClient.patch()
                .uri("/triage/pacientes/p-001/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"estado\": \"EN_CONSULTA\"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.error").isEqualTo("TRANSICION_INVALIDA");
    }

    @Test
    void patch_paciente_inexistente_devuelve_400() {
        when(cambiarEstadoUseCase.cambiarEstado(anyString(), anyString()))
                .thenReturn(Mono.error(new DominioException("id", "Paciente no encontrado")));

        webTestClient.patch()
                .uri("/triage/pacientes/no-existe/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"estado\": \"EN_CONSULTA\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void get_paciente_devuelve_200() {
        when(obtenerPacienteUseCase.obtener(anyString())).thenReturn(Mono.just(PACIENTE));

        webTestClient.get()
                .uri("/triage/pacientes/p-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("p-001")
                .jsonPath("$.estado").isEqualTo("EN_CONSULTA");
    }
}
