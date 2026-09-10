package com.juan.tfgplatform.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

    public enum Rol {
        PROFESOR,
        ALUMNO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private Rol rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // =============================
    // RELACIONES
    // =============================

    // Profesor → 1:N → Grupos
    @OneToMany(mappedBy = "profesor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Grupo> grupos = new ArrayList<>();

    // Grupo ↔ N:M ↔ Alumnos
    @ManyToMany(mappedBy = "alumnos")
    @JsonIgnore
    private Set<Grupo> gruposComoAlumno = new HashSet<>();

    // Profesor → 1:N → Ejercicios creados
    @OneToMany(mappedBy = "creador")
    @JsonIgnore
    private List<Ejercicio> ejerciciosCreados = new ArrayList<>();

    // =============================
    // CONSTRUCTOR
    // =============================

    public Usuario() {
    }

    // =============================
    // GETTERS Y SETTERS
    // =============================

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public List<Grupo> getGrupos() { return grupos; }
    public void setGrupos(List<Grupo> grupos) { this.grupos = grupos; }

    public Set<Grupo> getGruposComoAlumno() {
    return gruposComoAlumno;
}

public void setGruposComoAlumno(Set<Grupo> gruposComoAlumno) {
    this.gruposComoAlumno = gruposComoAlumno;
}

    public List<Ejercicio> getEjerciciosCreados() { return ejerciciosCreados; }
    public void setEjerciciosCreados(List<Ejercicio> ejerciciosCreados) { this.ejerciciosCreados = ejerciciosCreados; }

    @Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Usuario)) return false;
    Usuario usuario = (Usuario) o;
    return id != null && id.equals(usuario.id);
}

@Override
public int hashCode() {
    return getClass().hashCode();
}
}