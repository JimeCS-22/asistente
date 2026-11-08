package com.taller.asistente.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Cliente HTTP hacia el servidor local de Ollama (puerto 11434). */
@Component
public class OllamaClient {

    private final RestClient http;
    private final ObjectMapper mapper;
    private final String chatModel;
    private final String embedModel;

    public OllamaClient(@Value("${ollama.url}") String url,
                        @Value("${ollama.chat-model}") String chatModel,
                        @Value("${ollama.embed-model}") String embedModel,
                        @Value("${ollama.timeout-seconds}") int timeout,
                        ObjectMapper mapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(timeout));
        this.http = RestClient.builder().baseUrl(url).requestFactory(factory).build();
        this.mapper = mapper;
        this.chatModel = chatModel;
        this.embedModel = embedModel;
    }

    /** POST /api/embed -> vector semántico del texto. */
    public double[] embed(String texto) {
        JsonNode r = leerJson(http.post().uri("/api/embed")
                .body(Map.of("model", embedModel, "input", texto))
                .retrieve().body(String.class));
        JsonNode arr = r.get("embeddings").get(0);
        double[] v = new double[arr.size()];
        for (int i = 0; i < v.length; i++) v[i] = arr.get(i).asDouble();
        return v;
    }

    /**
     * POST /api/chat. Si "formato" no es null, Ollama queda obligado a
     * responder con ese esquema JSON (salida estructurada).
     */
    public String chat(String system, String user, JsonNode formato) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", chatModel);
        body.put("stream", false);
        body.put("messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)));
        body.put("options", Map.of("temperature", 0.1, "num_predict", 800));

        // ====== TODO 2 (Paso 2): agregar el parámetro "format" con el esquema JSON ======
        if (formato != null) {
            body.put("format", formato);
        }
        // ====== FIN TODO 2 ======

        JsonNode r = leerJson(http.post().uri("/api/chat").body(body)
                .retrieve().body(String.class));
        return r.path("message").path("content").asText();
    }

    /** Ollama a veces responde con un Content-Type no estándar: se lee como texto y se parsea. */
    private JsonNode leerJson(String texto) {
        try {
            return mapper.readTree(texto);
        } catch (JsonProcessingException e) {
            throw new RestClientException("La respuesta de Ollama no es JSON válido", e);
        }
    }
}