package com.juan.tfgplatform.dto;

import java.math.BigDecimal;

public class PreguntaRequestDTO {

    private String enunciado;
    private String tipo; 
    private BigDecimal puntuacionMaxima;
    private String configuracionJson;
    private BigDecimal tolerancia;
    private BigDecimal valorCorrecto;

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(BigDecimal puntuacionMaxima) { this.puntuacionMaxima = puntuacionMaxima; }

    public String getConfiguracionJson() { return configuracionJson; }
    public void setConfiguracionJson(String configuracionJson) { this.configuracionJson = configuracionJson; }

    
    public BigDecimal getValorCorrecto(){ return valorCorrecto; }
    public void setValorCorrecto(BigDecimal valorCorrecto){ this.valorCorrecto = valorCorrecto; }

    public BigDecimal getTolerancia(){ return tolerancia; }
    public void setTolerancia(BigDecimal tolerancia){ this.tolerancia = tolerancia; }
}