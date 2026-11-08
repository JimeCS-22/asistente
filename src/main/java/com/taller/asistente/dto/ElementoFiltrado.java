package com.taller.asistente.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ElementoFiltrado(Long id, String titulo, String motivo) {}