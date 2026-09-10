package com.juan.tfgplatform.controller;

import com.juan.tfgplatform.dto.RespuestaRequestDTO;
import com.juan.tfgplatform.dto.RespuestaResponseDTO;
import com.juan.tfgplatform.model.Respuesta;
import com.juan.tfgplatform.service.RespuestaService;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/respuestas")
public class RespuestaController {

    private final RespuestaService respuestaService;

    public RespuestaController(RespuestaService respuestaService) {
        this.respuestaService = respuestaService;
    }

    @PostMapping
    public RespuestaResponseDTO crear(@RequestBody RespuestaRequestDTO dto) {

        Respuesta respuesta = new Respuesta();
        respuesta.setRespuestaTexto(dto.getRespuestaTexto());
        respuesta.setRespuestaJson(dto.getRespuestaJson());
        respuesta.setImagenUrl(dto.getImagenUrl());
        respuesta.setPuntuacionObtenida(dto.getPuntuacionObtenida());

        Respuesta guardada = respuestaService.crearRespuesta(
                dto.getEntregaId(),
                dto.getPreguntaId(),
                respuesta
        );

        return convertirADTO(guardada);
    }


    @GetMapping
    public List<RespuestaResponseDTO> listar() {
        List<RespuestaResponseDTO> respuestaDTO = new ArrayList<>();
        for (Respuesta respuesta : respuestaService.listarTodas()) {
            respuestaDTO.add(convertirADTO(respuesta));
        }
        return respuestaDTO;
    }

    private RespuestaResponseDTO convertirADTO(Respuesta respuesta) {

        RespuestaResponseDTO dto = new RespuestaResponseDTO();

        dto.setId(respuesta.getId());
        dto.setEntregaId(respuesta.getEntrega().getId());
        dto.setPreguntaId(respuesta.getPregunta().getId());
        dto.setRespuestaTexto(respuesta.getRespuestaTexto());
        dto.setRespuestaJson(respuesta.getRespuestaJson());
        dto.setImagenUrl(respuesta.getImagenUrl());
        dto.setPuntuacionObtenida(respuesta.getPuntuacionObtenida());

        return dto;
    }
}