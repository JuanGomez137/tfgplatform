package com.juan.tfgplatform.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "preguntas")
public class Pregunta {

    public enum TipoPregunta {
        NUMERICO,
        TABLA,
        IMAGEN,
        TEXTO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private TipoPregunta tipo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal puntuacionMaxima;

    @Column(precision = 10, scale = 4)
    private BigDecimal valorCorrecto;

    @Column(precision = 5, scale = 4)
    private BigDecimal tolerancia;

    @Column(columnDefinition = "TEXT")
    private String configuracionJson;

    /**
     * Rúbrica de corrección para preguntas TEXTO e IMAGEN.
     * El profesor puede escribir aquí los criterios de evaluación y/o una
     * respuesta modelo correcta. Este texto se incluye en el prompt de la IA
     * para mejorar la precisión de la corrección automática.
     */
    @Column(columnDefinition = "TEXT")
    private String rubrica;

    // =============================
    // RELACIONES
    // =============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Respuesta> respuestas = new ArrayList<>();

    public Pregunta() {
    }

    // Getters y Setters

    public Long getId() { return id; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public TipoPregunta getTipo() { return tipo; }
    public void setTipo(TipoPregunta tipo) { this.tipo = tipo; }

    public BigDecimal getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(BigDecimal puntuacionMaxima) { this.puntuacionMaxima = puntuacionMaxima; }

    public String getConfiguracionJson() { return configuracionJson; }
    public void setConfiguracionJson(String configuracionJson) { this.configuracionJson = configuracionJson; }

    public Ejercicio getEjercicio() { return ejercicio; }
    public void setEjercicio(Ejercicio ejercicio) { this.ejercicio = ejercicio; }

    public List<Respuesta> getRespuestas() { return respuestas; }
    public void setRespuestas(List<Respuesta> respuestas) { this.respuestas = respuestas; }

    public BigDecimal getValorCorrecto(){ return valorCorrecto; }
    public void setValorCorrecto(BigDecimal valorCorrecto){ this.valorCorrecto = valorCorrecto; }

    public BigDecimal getTolerancia(){ return tolerancia; }
    public void setTolerancia(BigDecimal tolerancia){ this.tolerancia = tolerancia; }

    public String getRubrica() { return rubrica; }
    public void setRubrica(String rubrica) { this.rubrica = rubrica; }

}