package com.vidacruz.asistente.application.service;

import com.vidacruz.asistente.application.port.out.LlmPort;
import com.vidacruz.asistente.application.port.out.VectorStorePort;
import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsultarProtocoloServiceTest {

    private VectorStorePort vectorStore;
    private LlmPort llm;
    private ConsultarProtocoloService service;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStorePort.class);
        llm = mock(LlmPort.class);
        service = new ConsultarProtocoloService(vectorStore, llm);
    }

    @Test
    void con_fuentes_devuelve_respuesta_con_citas() {
        var fuente = new Fuente("PROTO-001", "Medidas iniciales");
        when(vectorStore.buscarSimilares(anyString(), anyInt())).thenReturn(Flux.just(fuente));
        when(llm.responder(anyString(), any())).thenReturn(Mono.just(
                new Respuesta("Reconfirmar la presión tras 5 minutos.", List.of(fuente))
        ));

        StepVerifier.create(service.consultar("¿Medidas iniciales en crisis hipertensiva?"))
                .assertNext(r -> {
                    assertThat(r.respuesta()).contains("Reconfirmar");
                    assertThat(r.fuentes()).hasSize(1);
                    assertThat(r.fuentes().get(0).protocolo()).isEqualTo("PROTO-001");
                })
                .verifyComplete();
    }

    @Test
    void sin_fuentes_devuelve_aviso_sin_contenido_clinico() {
        when(vectorStore.buscarSimilares(anyString(), anyInt())).thenReturn(Flux.empty());

        StepVerifier.create(service.consultar("¿Tratamiento para diabetes tipo 2?"))
                .assertNext(r -> {
                    assertThat(r.respuesta()).isEqualTo(Respuesta.MENSAJE_SIN_PROTOCOLO);
                    assertThat(r.fuentes()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void si_llm_no_cita_fuentes_se_reemplaza_por_aviso() {
        var fuente = new Fuente("PROTO-001", "Medidas iniciales");
        when(vectorStore.buscarSimilares(anyString(), anyInt())).thenReturn(Flux.just(fuente));
        // LLM "alucina": devuelve respuesta sin fuentes
        when(llm.responder(anyString(), any())).thenReturn(Mono.just(
                new Respuesta("Respuesta inventada", List.of())
        ));

        StepVerifier.create(service.consultar("¿Crisis hipertensiva?"))
                .assertNext(r -> {
                    assertThat(r.respuesta()).isEqualTo(Respuesta.MENSAJE_SIN_PROTOCOLO);
                    assertThat(r.fuentes()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void pregunta_vacia_devuelve_aviso() {
        StepVerifier.create(service.consultar(""))
                .assertNext(r -> assertThat(r.respuesta()).isEqualTo(Respuesta.MENSAJE_SIN_PROTOCOLO))
                .verifyComplete();
    }
}
