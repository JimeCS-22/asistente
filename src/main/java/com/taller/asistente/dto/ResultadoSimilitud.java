package com.taller.asistente.dto;

import com.taller.asistente.model.Pelicula;

public record ResultadoSimilitud(Pelicula pelicula, double similitud) {}
