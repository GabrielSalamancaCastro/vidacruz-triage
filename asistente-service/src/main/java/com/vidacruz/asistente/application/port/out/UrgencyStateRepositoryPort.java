package com.vidacruz.asistente.application.port.out;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface UrgencyStateRepositoryPort {

    /**
     * Incrementa el contador de un nivel si el paciente no estaba ya contado.
     *
     * @param pacienteId id del paciente
     * @param nivel      nivel de triaje (CRITICO, URGENTE, ...)
     * @return {@code Mono<Void>} que completa cuando se ha actualizado
     */
    Mono<Void> registrarPaciente(String pacienteId, String nivel);

    /**
     * Decrementa el contador de un nivel (paciente dado de alta).
     *
     * @param pacienteId id del paciente
     * @param nivel      nivel de triaje
     * @return {@code Mono<Void>} que completa cuando se ha actualizado
     */
    Mono<Void> darDeAltaPaciente(String pacienteId, String nivel);

    /**
     * Devuelve el conteo actual por nivel.
     *
     * @return {@code Mono<Map<String, Long>>} con el conteo por nivel
     */
    Mono<Map<String, Long>> obtenerEstado();
}
