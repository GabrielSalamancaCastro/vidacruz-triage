package com.vidacruz.asistente.infrastructure.adapter.out.redis;

import com.vidacruz.asistente.application.port.out.UrgencyStateRepositoryPort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class UrgencyStateRedisAdapter implements UrgencyStateRepositoryPort {

    private static final String PREFIX = "urgencias:";
    private static final List<String> NIVELES = List.of(
            "CRITICO", "URGENTE", "URGENTE_2", "NO_URGENTE", "NO_URGENTE_2"
    );

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public UrgencyStateRedisAdapter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> registrarPaciente(String pacienteId, String nivel) {
        var key = PREFIX + nivel;
        return redisTemplate.opsForHash()
                .put(key, pacienteId, "1")
                .then();
    }

    @Override
    public Mono<Void> darDeAltaPaciente(String pacienteId, String nivel) {
        var key = PREFIX + nivel;
        return redisTemplate.opsForHash()
                .remove(key, pacienteId)
                .then();
    }

    @Override
    public Mono<Map<String, Long>> obtenerEstado() {
        return reactor.core.publisher.Flux.fromIterable(NIVELES)
                .flatMap(nivel -> redisTemplate.opsForHash()
                        .size(PREFIX + nivel)
                        .map(count -> Map.entry(nivel, count))
                        .defaultIfEmpty(Map.entry(nivel, 0L)))
                .collectList()
                .map(entries -> {
                    Map<String, Long> resultado = new LinkedHashMap<>();
                    entries.forEach(e -> resultado.put(e.getKey(), e.getValue()));
                    return resultado;
                });
    }
}
