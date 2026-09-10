package com.juan.tfgplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EntregaResponseDTO {

    private Long id;
    private LocalDateTime fechaEntrega;
    private BigDecimal notaTotal;
    private String estado;
    private Long alumnoId;
    private Long ejercicioId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public BigDecimal getNotaTotal() { return notaTotal; }
    public void setNotaTotal(BigDecimal notaTotal) { this.notaTotal = notaTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }

    public Long getEjercicioId() { return ejercicioId; }
    public void setEjercicioId(Long ejercicioId) { this.ejercicioId = ejercicioId; }
}