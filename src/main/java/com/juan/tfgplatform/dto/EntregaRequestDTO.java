package com.juan.tfgplatform.dto;

public class EntregaRequestDTO {

    private Long alumnoId;
    private Long ejercicioId;

    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }

    public Long getEjercicioId() { return ejercicioId; }
    public void setEjercicioId(Long ejercicioId) { this.ejercicioId = ejercicioId; }
}