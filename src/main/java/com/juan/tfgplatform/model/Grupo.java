package com.juan.tfgplatform.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;


@Entity
@Table(
    name = "grupos",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nombre", "profesor_id"})
    }
)
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // =============================
    // RELACIONES
    // =============================

    // Grupo → Profesor (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor;

    // Grupo ↔ N:M ↔ Alumnos
    @ManyToMany
    @JoinTable(
        name = "grupo_alumnos",
        joinColumns = @JoinColumn(name = "grupo_id"),
        inverseJoinColumns = @JoinColumn(name = "alumno_id")
    )
    
    @JsonIgnore
    private Set<Usuario> alumnos = new HashSet<>();

    // Grupo ↔ N:M ↔ Ejercicios
    @ManyToMany
    @JoinTable(
        name = "grupo_ejercicios",
        joinColumns = @JoinColumn(name = "grupo_id"),
        inverseJoinColumns = @JoinColumn(name = "ejercicio_id")
    )
    @JsonIgnore
    private Set<Ejercicio> ejercicios = new HashSet<>();

    // =============================
    // CONSTRUCTOR
    // =============================

    public Grupo() {
    }
// =============================
// MÉTODOS HELPER PARA N:M
// =============================

public void agregarAlumno(Usuario alumno) {
    this.alumnos.add(alumno);
    alumno.getGruposComoAlumno().add(this);
}

public void quitarAlumno(Usuario alumno) {
    this.alumnos.remove(alumno);
    alumno.getGruposComoAlumno().remove(this);
}

public void agregarEjercicio(Ejercicio ejercicio) {
    this.ejercicios.add(ejercicio);
    ejercicio.getGrupos().add(this);
}

public void quitarEjercicio(Ejercicio ejercicio) {
    this.ejercicios.remove(ejercicio);
    ejercicio.getGrupos().remove(this);
}
    // =============================
    // GETTERS Y SETTERS
    // =============================

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Usuario getProfesor() { return profesor; }
    public void setProfesor(Usuario profesor) { this.profesor = profesor; }

   public Set<Usuario> getAlumnos() {
    return alumnos;
}

public void setAlumnos(Set<Usuario> alumnos) {
    this.alumnos = alumnos;
}

public Set<Ejercicio> getEjercicios() {
    return ejercicios;
}

public void setEjercicios(Set<Ejercicio> ejercicios) {
    this.ejercicios = ejercicios;
}
    @Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Grupo)) return false;
    Grupo grupo = (Grupo) o;
    return id != null && id.equals(grupo.id);
}

@Override
public int hashCode() {
    return getClass().hashCode();
}
}