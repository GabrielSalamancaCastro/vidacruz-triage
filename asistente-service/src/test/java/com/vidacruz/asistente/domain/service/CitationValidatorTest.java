package com.vidacruz.asistente.domain.service;

import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CitationValidatorTest {

    private final CitationValidator validator = new CitationValidator();

    @Test
    void respuesta_con_fuente_valida_se_acepta() {
        var fuente = new Fuente("PROTO-001", "Medidas iniciales");
        var respuesta = new Respuesta("Contenido clínico [PROTO-001]", List.of(fuente));

        var resultado = validator.validar(respuesta, List.of(fuente));

        assertThat(resultado.respuesta()).isEqualTo(respuesta.respuesta());
        assertThat(resultado.fuentes()).hasSize(1);
    }

    @Test
    void respuesta_sin_fuentes_se_reemplaza_por_aviso() {
        var fuente = new Fuente("PROTO-001", "Medidas iniciales");
        var respuesta = new Respuesta("Contenido clínico", List.of());

        var resultado = validator.validar(respuesta, List.of(fuente));

        assertThat(resultado.respuesta()).isEqualTo(Respuesta.MENSAJE_SIN_PROTOCOLO);
        assertThat(resultado.fuentes()).isEmpty();
    }

    @Test
    void respuesta_con_fuente_inventada_se_reemplaza_por_aviso() {
        var fuenteReal = new Fuente("PROTO-001", "Medidas iniciales");
        var fuenteInventada = new Fuente("PROTO-999", "Sección inventada");
        var respuesta = new Respuesta("Contenido clínico [PROTO-999]", List.of(fuenteInventada));

        var resultado = validator.validar(respuesta, List.of(fuenteReal));

        assertThat(resultado.respuesta()).isEqualTo(Respuesta.MENSAJE_SIN_PROTOCOLO);
        assertThat(resultado.fuentes()).isEmpty();
    }

    @Test
    void respuesta_nula_se_reemplaza_por_aviso() {
        var resultado = validator.validar(null, List.of());

        assertThat(resultado.respuesta()).isEqualTo(Respuesta.MENSAJE_SIN_PROTOCOLO);
    }
}
