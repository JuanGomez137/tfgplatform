package com.juan.tfgplatform.dto;

import java.util.Set;

public class GrupoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Long profesorId;
    private Set<Long> alumnosIds;
    private Set<Long> ejerciciosIds;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getProfesorId() { return profesorId; }
    public void setProfesorId(Long profesorId) { this.profesorId = profesorId; }

    public Set<Long> getAlumnosIds() { return alumnosIds; }
    public void setAlumnosIds(Set<Long> alumnosIds) { this.alumnosIds = alumnosIds; }

    public Set<Long> getEjerciciosIds() { return ejerciciosIds; }
    public void setEjerciciosIds(Set<Long> ejerciciosIds) { this.ejerciciosIds = ejerciciosIds; }
}