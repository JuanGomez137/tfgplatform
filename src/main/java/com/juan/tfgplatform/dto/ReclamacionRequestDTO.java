package com.juan.tfgplatform.dto;

public class ReclamacionRequestDTO {

    private Long entregaId;
    private Long respuestaId;
    private String comentario;

    public Long getEntregaId(){ return entregaId; }
    public void setEntregaId(Long entregaId){ this.entregaId = entregaId; }

    public Long getRespuestaId(){ return respuestaId; }
    public void setRespuestaId(Long respuestaId){ this.respuestaId = respuestaId; }

    public String getComentario(){ return comentario; }
    public void setComentario(String comentario){ this.comentario = comentario; }
}
