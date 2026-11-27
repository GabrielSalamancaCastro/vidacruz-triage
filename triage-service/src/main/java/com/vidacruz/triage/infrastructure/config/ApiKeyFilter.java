package com.vidacruz.triage.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "triage.security.enabled", havingValue = "true", matchIfMissing = true)
public class ApiKeyFilter implements WebFilter{

    private static final String HEADER = "X-API-Key";

    @Value("${triage.security.api-key}")
    private String apiKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var path = exchange.getRequest().getPath().value();

        // Excluir Actuator (health, metrics, prometheus)
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        var provided = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (apiKey.equals(provided)) {
            return chain.filter(exchange);
        }

        // 401 Unauthorized
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {"timestamp":"%s","error":"NO_AUTORIZADO","mensaje":"Header X-API-Key requerido o inválido"}
                """.formatted(Instant.now());

        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
