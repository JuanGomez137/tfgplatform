package com.juan.tfgplatform.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "ejercicios")
public class Ejercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private LocalDateTime fechaPublicacion;

    private LocalDateTime fechaLimite;

    // =============================
    // RELACIONES
    // =============================

    // Profesor creador (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    // N:M con Grupo
    @ManyToMany(mappedBy = "ejercicios")
    @JsonIgnore
    private Set<Grupo> grupos = new HashSet<>();

    // 1:N con Pregunta
    @OneToMany(mappedBy = "ejercicio", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Pregunta> preguntas = new ArrayList<>();

    // 1:N con Entrega
    @OneToMany(mappedBy = "ejercicio")
    @JsonIgnore
    private List<Entrega> entregas = new ArrayList<>();

    public Ejercicio() {
    }

    // Getters y Setters

    public Long getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public LocalDateTime getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDateTime fechaLimite) { this.fechaLimite = fechaLimite; }

    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }

    public Set<Grupo> getGrupos() {
    return grupos;
}

public void setGrupos(Set<Grupo> grupos) {
    this.grupos = grupos;
}

    public List<Pregunta> getPreguntas() { return preguntas; }
    public void setPreguntas(List<Pregunta> preguntas) { this.preguntas = preguntas; }

    public List<Entrega> getEntregas() { return entregas; }
    public void setEntregas(List<Entrega> entregas) { this.entregas = entregas; }

    @Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Ejercicio)) return false;
    Ejercicio ejercicio = (Ejercicio) o;
    return id != null && id.equals(ejercicio.id);
}

@Override
public int hashCode() {
    return getClass().hashCode();
}
}