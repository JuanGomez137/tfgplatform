package com.juan.tfgplatform.dto;

import java.math.BigDecimal;

public class RespuestaResponseDTO {

    private Long id;
    private Long entregaId;
    private Long preguntaId;

    private String respuestaTexto;
    private String respuestaJson;
    private String imagenUrl;

    private BigDecimal puntuacionObtenida;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEntregaId() { return entregaId; }
    public void setEntregaId(Long entregaId) { this.entregaId = entregaId; }

    public Long getPreguntaId() { return preguntaId; }
    public void setPreguntaId(Long preguntaId) { this.preguntaId = preguntaId; }

    public String getRespuestaTexto() { return respuestaTexto; }
    public void setRespuestaTexto(String respuestaTexto) { this.respuestaTexto = respuestaTexto; }

    public String getRespuestaJson() { return respuestaJson; }
    public void setRespuestaJson(String respuestaJson) { this.respuestaJson = respuestaJson; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public BigDecimal getPuntuacionObtenida() { return puntuacionObtenida; }
    public void setPuntuacionObtenida(BigDecimal puntuacionObtenida) { this.puntuacionObtenida = puntuacionObtenida; }
}