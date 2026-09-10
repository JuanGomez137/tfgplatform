package com.juan.tfgplatform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamaciones")
public class Reclamacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comentario;

    @Column(columnDefinition = "TEXT")
    private String comentarioProfesor;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private Boolean resuelta = false;

    // =============================
    // RELACIONES
    // =============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrega_id", nullable = false)
    private Entrega entrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id", nullable = false)
    private Respuesta respuesta;

    public Reclamacion() {
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getComentarioProfesor() { return comentarioProfesor; }
    public void setComentarioProfesor(String comentarioProfesor) { this.comentarioProfesor = comentarioProfesor; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public Boolean getResuelta() { return resuelta; }
    public void setResuelta(Boolean resuelta) { this.resuelta = resuelta; }

    public Entrega getEntrega() { return entrega; }
    public void setEntrega(Entrega entrega) { this.entrega = entrega; }

    public Respuesta getRespuesta() { return respuesta; }
    public void setRespuesta(Respuesta respuesta) { this.respuesta = respuesta; }
}