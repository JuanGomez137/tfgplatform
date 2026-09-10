package com.juan.tfgplatform.dto;

import java.time.LocalDateTime;

public class ReclamacionResponseDTO {

    private Long id;
    private String comentario;
    private String comentarioProfesor;
    private LocalDateTime fechaCreacion;
    private Boolean resuelta;
    private Long entregaId;
    private Long respuestaId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getResuelta() {
        return resuelta;
    }

    public void setResuelta(Boolean resuelta) {
        this.resuelta = resuelta;
    }

    public Long getEntregaId() { return entregaId; }
    public void setEntregaId(Long entregaId) { this.entregaId = entregaId; }

    public Long getRespuestaId() { return respuestaId; }
    public void setRespuestaId(Long respuestaId) { this.respuestaId = respuestaId; }

    public String getComentarioProfesor() { return comentarioProfesor; }
    public void setComentarioProfesor(String comentarioProfesor) { this.comentarioProfesor = comentarioProfesor; }
}