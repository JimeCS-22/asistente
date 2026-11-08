package com.taller.asistente.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.asistente.dto.ConsultaResponse;
import com.taller.asistente.dto.RespuestaIA;
import com.taller.asistente.dto.ResultadoSimilitud;
import com.taller.asistente.Repository.PeliculaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusquedaService {

    private static final int TOP_K = 4;

    private static final String SYSTEM = """
            Eres un analista de datos organizacionales. Respondes en español.
            Usa EXCLUSIVAMENTE los registros del contexto. Si algo no está en el contexto, no lo inventes.
            """;

    /** Esquema JSON que se le exige a Ollama (parámetro format). */
    private static final String ESQUEMA = """
            {
              "type": "object",
              "properties": {
                "resumen_ejecutivo": {"type": "string"},
                "elementos_filtrados": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "id": {"type": "integer"},
                      "titulo": {"type": "string"},
                      "motivo": {"type": "string"}
                    },
                    "required": ["id", "titulo", "motivo"]
                  }
                },
                "recomendacion": {"type": "string"}
              },
              "required": ["resumen_ejecutivo", "elementos_filtrados", "recomendacion"]
            }
            """;

    private final PeliculaRepository repo;
    private final IndiceVectorial indice;
    private final SimilitudService similitud;
    private final OllamaClient ollama;
    private final ObjectMapper mapper;

    public BusquedaService(PeliculaRepository repo, IndiceVectorial indice, SimilitudService similitud,
            OllamaClient ollama, ObjectMapper mapper) {
        this.repo = repo;
        this.indice = indice;
        this.similitud = similitud;
        this.ollama = ollama;
        this.mapper = mapper;
    }

    /** Flujo RAG completo. */
    public ConsultaResponse consultar(String pregunta) {
        if (pregunta == null || pregunta.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pregunta está vacía");
        if (!indice.estaListo())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El índice vectorial no está listo. Revisa que Ollama esté corriendo.");
        try {
            // 1) Vectorizar la pregunta
            double[] vPregunta = ollama.embed(pregunta);

            // 2) Similitud coseno contra todos los registros y top-k
            List<ResultadoSimilitud> top = repo.findAll().stream()
                    .map(p -> new ResultadoSimilitud(p,
                            similitud.coseno(vPregunta, indice.getVectores().get(p.getId()))))
                    .sorted(Comparator.comparingDouble(ResultadoSimilitud::similitud).reversed())
                    .limit(TOP_K)
                    .toList();

            // 3) Prompt Augmentation: inyectar los registros reales como contexto
            String contexto = top.stream().map(r -> r.pelicula().lineaContexto())
                    .collect(Collectors.joining("\n"));
            String prompt = "Contexto (datos reales de la base de datos):\n" + contexto
                    + "\n\nPregunta: " + pregunta
                    + "\n\nResponde ÚNICAMENTE con un objeto JSON con exactamente estos campos: "
                    + "resumen_ejecutivo (texto), elementos_filtrados (lista de objetos con id, titulo y motivo, "
                    + "usando solo películas del contexto) y recomendacion (texto). "
                    + "No agregues texto fuera del JSON.";

            // 4) Llamar a Ollama con salida estructurada y parsear al DTO
            String json = ollama.chat(SYSTEM, prompt, mapper.readTree(ESQUEMA));
            RespuestaIA analisis = mapper.readValue(json, RespuestaIA.class);
            return new ConsultaResponse(analisis, top);

        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "El modelo devolvió un JSON inválido");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar a Ollama: " + e.getMessage());
        }
    }

    /**
     * Solo para el gancho de la demo: la MISMA pregunta sin RAG (el modelo
     * inventa).
     */
    public String consultarSinRag(String pregunta) {
        try {
            return ollama.chat("Responde en español, de forma breve.", pregunta, null);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo contactar a Ollama");
        }
    }
}