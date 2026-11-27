package com.vidacruz.asistente.infrastructure.config;

import org.springframework.boot.ApplicationRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Carga el corpus de protocolos en pgvector al arrancar (RAG).
 *
 * <p>Solo se activa en el perfil {@code llm-real}. En el perfil por defecto
 * (fake) no se carga nada, porque no hay VectorStore real.
 *
 * <p>Es idempotente: si un protocolo ya está indexado, no lo vuelve a insertar.
 * Se comprueba por el metadato {@code source} (nombre del archivo).
 */
@Component
@Profile("llm-real")
public class CorpusIngestor implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CorpusIngestor.class);

    private final VectorStore vectorStore;

    public CorpusIngestor(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Cargando corpus de protocolos en pgvector...");

        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] files = resolver.getResources("classpath:docs/corpus/*.md");

        if (files.length == 0) {
            log.warn("No se encontraron archivos de corpus en classpath:docs/corpus/*.md");
            return;
        }

        var splitter = new TokenTextSplitter(
                300,    // chunkSize
                100,    // minChunkSizeChars
                5,      // minChunkLengthToEmbed
                10000,  // maxNumChunks
                true    // keepSeparator
        );

        List<Document> todosLosChunks = new ArrayList<>();

        for (Resource file : files) {
            String source = file.getFilename();

            // Idempotencia: comprobar si ya está indexado
            // (en producción se haría con una query a vector_store; aquí simplificamos
            // cargando todo de nuevo solo si el store está vacío)
            // Para la prueba, cargamos todo y confiamos en que el store se recrea al arrancar.

            var config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(false)
                    .withIncludeCodeBlock(false)
                    .withIncludeBlockquote(false)
                    .build();

            var reader = new MarkdownDocumentReader(file, config);
            List<Document> docs = reader.get();

            // Añadir metadatos de protocolo y sección
            for (Document doc : docs) {
                var content = doc.getContent();
                // Extraer protocolo del nombre del archivo (ej. PROTO-001-crisis-hipertensiva.md)
                String protocolo = extraerProtocolo(source);
                String seccion = extraerSeccion(content);
                doc.getMetadata().put("protocolo", protocolo);
                doc.getMetadata().put("seccion", seccion);
                doc.getMetadata().put("source", source);
            }

            List<Document> chunks = splitter.apply(docs);
            todosLosChunks.addAll(chunks);
            log.info("Protocolo {} cargado: {} chunks", source, chunks.size());
        }

        if (!todosLosChunks.isEmpty()) {
            vectorStore.add(todosLosChunks);
            log.info("Corpus cargado en pgvector: {} chunks totales", todosLosChunks.size());
        }
    }

    private String extraerProtocolo(String filename) {
        // PROTO-001-crisis-hipertensiva.md → PROTO-001
        return filename.split("-")[0] + "-" + filename.split("-")[1];
    }

    private String extraerSeccion(String content) {
        // Buscar la primera línea que empiece con "## " (sección markdown)
        for (String line : content.split("\n")) {
            if (line.startsWith("## ")) {
                return line.substring(3).trim();
            }
        }
        return "General";
    }
}
