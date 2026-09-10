package com.juan.tfgplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "respuestas",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"entrega_id", "pregunta_id"})
    }
)
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String respuestaTexto;

    @Column(columnDefinition = "TEXT")
    private String respuestaJson;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "feedback_ia", columnDefinition = "TEXT")
    private String feedbackIa;

    @Column(precision = 5, scale = 2)
    private BigDecimal puntuacionObtenida;

    // =============================
    // RELACIONES
    // =============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrega_id", nullable = false)
    private Entrega entrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @OneToMany(mappedBy = "respuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reclamacion> reclamaciones = new ArrayList<>();

    public Respuesta() {
    }

    // Getters y Setters

    public Long getId() { return id; }

    public String getRespuestaTexto() { return respuestaTexto; }
    public void setRespuestaTexto(String respuestaTexto) { this.respuestaTexto = respuestaTexto; }

    public String getRespuestaJson() { return respuestaJson; }
    public void setRespuestaJson(String respuestaJson) { this.respuestaJson = respuestaJson; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getFeedbackIa() { return feedbackIa; }
    public void setFeedbackIa(String feedbackIa) { this.feedbackIa = feedbackIa; }

    public BigDecimal getPuntuacionObtenida() { return puntuacionObtenida; }
    public void setPuntuacionObtenida(BigDecimal puntuacionObtenida) { this.puntuacionObtenida = puntuacionObtenida; }

    public Entrega getEntrega() { return entrega; }
    public void setEntrega(Entrega entrega) { this.entrega = entrega; }

    public Pregunta getPregunta() { return pregunta; }
    public void setPregunta(Pregunta pregunta) { this.pregunta = pregunta; }
}