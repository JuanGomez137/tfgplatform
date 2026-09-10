package com.juan.tfgplatform.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "entregas",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"alumno_id", "ejercicio_id"})
    }
)
public class Entrega {

    public enum EstadoEntrega {
        EN_PROGRESO,
        ENVIADA,
        PENDIENTE_IA,
        CORREGIDO_AUTO,
        EN_RECLAMACION,
        CORREGIDO_MANUAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaEntrega;

    @Column(precision = 5, scale = 2)
    private BigDecimal notaTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(30)")
    private EstadoEntrega estado;

    @Column(nullable = false)
    private Boolean visibleAlumno = false;

    // =============================
    // RELACIONES
    // =============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    @JsonIgnore
    private Usuario alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    @JsonIgnore
    private Ejercicio ejercicio;

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Respuesta> respuestas = new ArrayList<>();

    public Entrega() {
    }

    // Getters y Setters

    public Long getId() { return id; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public BigDecimal getNotaTotal() { return notaTotal; }
    public void setNotaTotal(BigDecimal notaTotal) { this.notaTotal = notaTotal; }

    public EstadoEntrega getEstado() { return estado; }
    public void setEstado(EstadoEntrega estado) { this.estado = estado; }

    public Usuario getAlumno() { return alumno; }
    public void setAlumno(Usuario alumno) { this.alumno = alumno; }

    public Ejercicio getEjercicio() { return ejercicio; }
    public void setEjercicio(Ejercicio ejercicio) { this.ejercicio = ejercicio; }

    public Boolean getVisibleAlumno() { return visibleAlumno; }
    public void setVisibleAlumno(Boolean visibleAlumno) { this.visibleAlumno = visibleAlumno; }

    public List<Respuesta> getRespuestas() { return respuestas; }
    public void setRespuestas(List<Respuesta> respuestas) { this.respuestas = respuestas; }
}