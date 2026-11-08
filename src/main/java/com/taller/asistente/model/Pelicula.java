package com.taller.asistente.model;

import jakarta.persistence.*;

@Entity
public class Pelicula {
    @Id
    private Long id;
    private String titulo;
    private String genero;
    private Integer anio;
    private Double recaudacionMillones;
    private String clasificacion;
    @Column(length = 1000)
    private String sinopsis;

    public Pelicula() {}

    /** Texto que se convierte en vector (el "chunk" de este registro). */
    public String textoParaEmbedding() {
        return titulo + ". Género: " + genero + ". Clasificación: " + clasificacion + ". " + sinopsis;
    }

    /** Línea de contexto que se inyecta en el prompt (Prompt Augmentation). */
    public String lineaContexto() {
        return "id=" + id + " | " + titulo + " | " + genero + " | " + anio + " | recaudación: "
                + recaudacionMillones + " millones USD | clasificación: " + clasificacion + " | " + sinopsis;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public Double getRecaudacionMillones() { return recaudacionMillones; }
    public void setRecaudacionMillones(Double r) { this.recaudacionMillones = r; }
    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String c) { this.clasificacion = c; }
    public String getSinopsis() { return sinopsis; }
    public void setSinopsis(String s) { this.sinopsis = s; }
}