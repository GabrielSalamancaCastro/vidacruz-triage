package com.vidacruz.asistente.infrastructure.config;

import com.vidacruz.asistente.application.port.out.LlmPort;
import com.vidacruz.asistente.application.port.out.VectorStorePort;
import com.vidacruz.asistente.application.service.ConsultarProtocoloService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsistenteConfig {

    @Bean
    public ConsultarProtocoloService consultarProtocoloService(
            VectorStorePort vectorStore,
            LlmPort llm
    ) {
        return new ConsultarProtocoloService(vectorStore, llm);
    }
}
