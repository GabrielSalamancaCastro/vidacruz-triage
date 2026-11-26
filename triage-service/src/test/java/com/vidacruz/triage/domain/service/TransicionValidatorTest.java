package com.vidacruz.triage.domain.service;

import com.vidacruz.triage.domain.exception.TransicionInvalidaException;
import com.vidacruz.triage.domain.model.EstadoPaciente;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class TransicionValidatorTest {
    private final TransicionValidator validator = new TransicionValidator();

    @Test
    void registrado_a_en_consulta_es_valida() {
        assertThatCode(() -> validator.validar(EstadoPaciente.REGISTRADO, EstadoPaciente.EN_CONSULTA))
                .doesNotThrowAnyException();
    }

    @Test
    void registrado_a_alta_es_valida() {
        assertThatCode(() -> validator.validar(EstadoPaciente.REGISTRADO, EstadoPaciente.ALTA))
                .doesNotThrowAnyException();
    }

    @Test
    void en_consulta_a_alta_es_valida() {
        assertThatCode(() -> validator.validar(EstadoPaciente.EN_CONSULTA, EstadoPaciente.ALTA))
                .doesNotThrowAnyException();
    }

    @Test
    void alta_a_en_consulta_es_invalida() {
        assertThatThrownBy(() -> validator.validar(EstadoPaciente.ALTA, EstadoPaciente.EN_CONSULTA))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void en_consulta_a_registrado_es_invalida() {
        assertThatThrownBy(() -> validator.validar(EstadoPaciente.EN_CONSULTA, EstadoPaciente.REGISTRADO))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void estado_nuevo_nulo_es_invalido() {
        assertThatThrownBy(() -> validator.validar(EstadoPaciente.REGISTRADO, null))
                .isInstanceOf(TransicionInvalidaException.class);
    }
}
