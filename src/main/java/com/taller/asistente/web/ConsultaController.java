package com.taller.asistente.web;

import com.taller.asistente.dto.ConsultaRequest;
import com.taller.asistente.dto.ConsultaResponse;
import com.taller.asistente.model.Pelicula;
import com.taller.asistente.Repository.PeliculaRepository;
import com.taller.asistente.service.BusquedaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class ConsultaController {

    private final BusquedaService busqueda;
    private final PeliculaRepository repo;

    public ConsultaController(BusquedaService busqueda, PeliculaRepository repo) {
        this.busqueda = busqueda;
        this.repo = repo;
    }

    @GetMapping("/peliculas")
    public List<Pelicula> listar() { return repo.findAll(); }

    /** Con RAG + salida estructurada. */
    @PostMapping("/consultas")
    public ConsultaResponse consultar(@RequestBody ConsultaRequest req) {
        return busqueda.consultar(req.pregunta());
    }

    /** Sin RAG (solo para el gancho: muestra cómo el modelo inventa). */
    @PostMapping("/consultas/sin-rag")
    public Map<String, String> sinRag(@RequestBody ConsultaRequest req) {
        return Map.of("respuesta", busqueda.consultarSinRag(req.pregunta()));
    }
}