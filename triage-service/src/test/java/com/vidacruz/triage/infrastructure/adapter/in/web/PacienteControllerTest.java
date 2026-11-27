package com.vidacruz.triage.infrastructure.adapter.in.web;

import com.vidacruz.triage.application.port.in.RegistrarPacienteUseCase;
import com.vidacruz.triage.domain.exception.DominioException;
import com.vidacruz.triage.domain.model.EstadoPaciente;
import com.vidacruz.triage.domain.model.NivelTriaje;
import com.vidacruz.triage.domain.model.Paciente;
import com.vidacruz.triage.domain.model.SignosVitales;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PacienteController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "triage.security.enabled=false") // -> Se añade solamente para que los test evaluen el comportamiento de los endpoints sin API KEY
public class PacienteControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RegistrarPacienteUseCase registrarPacienteUseCase;

    private static final String JSON_VALIDO = """
            {
              "nombreCompleto": "Juan Pérez",
              "edad": 45,
              "frecuenciaCardiaca": 130,
              "presionSistolica": 180,
              "presionDiastolica": 110,
              "spo2": 97,
              "temperatura": 36.8,
              "dolor": 8,
              "gcs": 15,
              "quejaPrincipal": "Dolor torácico"
            }
            """;

    @Test
    void registra_paciente_y_devuelve_201() {
        var paciente = new Paciente(
                "uuid-123",
                "Juan Pérez",
                45,
                new SignosVitales(130, 180, 110, 97, 36.8, 8, 15),
                "Dolor torácico",
                NivelTriaje.CRITICO,
                EstadoPaciente.REGISTRADO,
                false,
                Instant.parse("2026-10-29T15:04:05Z")
        );
        when(registrarPacienteUseCase.registrar(anyString(), anyInt(), any(SignosVitales.class), anyString()))
                .thenReturn(Mono.just(paciente));

        webTestClient.post()
                .uri("/triage/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JSON_VALIDO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("uuid-123")
                .jsonPath("$.nivel").isEqualTo("CRITICO")
                .jsonPath("$.slaMinutos").isEqualTo(5)
                .jsonPath("$.estado").isEqualTo("REGISTRADO")
                .jsonPath("$.fechaRegistro").exists();
    }

    @Test
    void datos_invalidos_devuelven_400() {
        // frecuenciaCardiaca = 10 (fuera de rango)
        String jsonInvalido = JSON_VALIDO.replace("\"frecuenciaCardiaca\": 130", "\"frecuenciaCardiaca\": 10");

        webTestClient.post()
                .uri("/triage/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonInvalido)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("VALIDACION_REQUEST");
    }

    @Test
    void dominio_exception_devuelve_400_con_campo() {
        when(registrarPacienteUseCase.registrar(anyString(), anyInt(), any(SignosVitales.class), anyString()))
                .thenReturn(Mono.error(new DominioException("presionDiastolica",
                        "La presión diastólica debe ser menor que la sistólica")));

        // JSON con diastólica > sistólica (válido para Jakarta, inválido para dominio)
        String jsonDiastolicaMayor = JSON_VALIDO
                .replace("\"presionSistolica\": 180", "\"presionSistolica\": 100")
                .replace("\"presionDiastolica\": 110", "\"presionDiastolica\": 110");

        webTestClient.post()
                .uri("/triage/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonDiastolicaMayor)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("VALIDACION_DOMINIO")
                .jsonPath("$.campo").isEqualTo("presionDiastolica");
    }
}
