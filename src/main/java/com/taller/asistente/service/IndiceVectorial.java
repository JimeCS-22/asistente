package com.taller.asistente.service;

import com.taller.asistente.model.Pelicula;
import com.taller.asistente.Repository.PeliculaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Ingesta: al arrancar, convierte cada registro de la BD en un vector y lo guarda en memoria. */
@Service
public class IndiceVectorial {

    private static final Logger log = LoggerFactory.getLogger(IndiceVectorial.class);
    private final Map<Long, double[]> vectores = new ConcurrentHashMap<>();
    private final PeliculaRepository repo;
    private final OllamaClient ollama;

    public IndiceVectorial(PeliculaRepository repo, OllamaClient ollama) {
        this.repo = repo;
        this.ollama = ollama;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void construir() {
        try {
            for (Pelicula p : repo.findAll()) {
                vectores.put(p.getId(), ollama.embed(p.textoParaEmbedding()));
            }
            log.info("Índice vectorial listo: {} películas", vectores.size());
        } catch (Exception e) {
            log.error("No se pudo construir el índice. ¿Ollama corre y tiene nomic-embed-text? {}", e.getMessage());
        }
    }

    public Map<Long, double[]> getVectores() { return vectores; }
    public boolean estaListo() { return !vectores.isEmpty(); }
}