package com.vidacruz.triage.domain.service;

import com.vidacruz.triage.domain.exception.TransicionInvalidaException;
import com.vidacruz.triage.domain.model.EstadoPaciente;

import java.util.Map;
import java.util.Set;

/**
 * Valida transiciones de estado según RF-02.
 *
 * Transiciones válidas:
 * - REGISTRADO → EN_CONSULTA
 * - REGISTRADO → ALTA
 * - EN_CONSULTA → ALTA
 *
 * Cualquier otra → TransicionInvalidaException → 409.
 */

public class TransicionValidator {
    private static final Map<EstadoPaciente, Set<EstadoPaciente>> TRANSICIONES_VALIDAS = Map.of(
            EstadoPaciente.REGISTRADO, Set.of(EstadoPaciente.EN_CONSULTA, EstadoPaciente.ALTA),
            EstadoPaciente.EN_CONSULTA, Set.of(EstadoPaciente.ALTA),
            EstadoPaciente.ALTA, Set.of()
    );

    public void validar(EstadoPaciente actual, EstadoPaciente nuevo) {
        if (nuevo == null) {
            throw new TransicionInvalidaException("El estado nuevo es obligatorio");
        }
        if (!TRANSICIONES_VALIDAS.get(actual).contains(nuevo)) {
            throw new TransicionInvalidaException(
                    "Transición inválida: " + actual + " → " + nuevo +
                            ". Transiciones permitidas desde " + actual + ": " +
                            TRANSICIONES_VALIDAS.get(actual));
        }
    }
}
