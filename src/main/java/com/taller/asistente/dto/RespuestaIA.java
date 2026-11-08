package com.taller.asistente.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Contrato JSON: el mismo esquema que se le exige a Ollama y que lee Angular. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RespuestaIA(
        @JsonProperty("resumen_ejecutivo") String resumenEjecutivo,
        @JsonProperty("elementos_filtrados") List<ElementoFiltrado> elementosFiltrados,
        @JsonProperty("recomendacion") String recomendacion) {}
