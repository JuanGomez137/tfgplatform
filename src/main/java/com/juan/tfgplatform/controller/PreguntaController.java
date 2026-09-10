package com.juan.tfgplatform.controller;

import com.juan.tfgplatform.dto.PreguntaRequestDTO;
import com.juan.tfgplatform.dto.PreguntaResponseDTO;
import com.juan.tfgplatform.model.Pregunta;
import com.juan.tfgplatform.service.PreguntaService;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    // =============================
    // CREAR
    // =============================

    @PostMapping("/ejercicio/{ejercicioId}")
    public PreguntaResponseDTO crear(@PathVariable Long ejercicioId,
                                     @RequestBody PreguntaRequestDTO dto) {

        Pregunta pregunta = new Pregunta();
        pregunta.setEnunciado(dto.getEnunciado());
        pregunta.setTipo(Pregunta.TipoPregunta.valueOf(dto.getTipo()));
        pregunta.setPuntuacionMaxima(dto.getPuntuacionMaxima());
        pregunta.setConfiguracionJson(dto.getConfiguracionJson());
        pregunta.setValorCorrecto(dto.getValorCorrecto());
        pregunta.setTolerancia(dto.getTolerancia());

        Pregunta guardada = preguntaService.crearPregunta(ejercicioId, pregunta);

        return convertirADTO(guardada);
    }

    // =============================
    // LISTAR
    // =============================

    @GetMapping
    public List<PreguntaResponseDTO> listar() {

        List<Pregunta> preguntas = preguntaService.listarTodas();
        List<PreguntaResponseDTO> respuesta = new ArrayList<>();

        for (Pregunta pregunta : preguntas) {
            respuesta.add(convertirADTO(pregunta));
        }

        return respuesta;
    }

    // =============================
    // MÉTODO PRIVADO DE CONVERSIÓN
    // =============================

    private PreguntaResponseDTO convertirADTO(Pregunta pregunta) {

    PreguntaResponseDTO dto = new PreguntaResponseDTO();

    dto.setId(pregunta.getId());
    dto.setEnunciado(pregunta.getEnunciado());
    dto.setTipo(pregunta.getTipo().name());
    dto.setPuntuacionMaxima(pregunta.getPuntuacionMaxima());
    dto.setConfiguracionJson(pregunta.getConfiguracionJson());
    dto.setTolerancia(pregunta.getTolerancia());
    dto.setValorCorrecto(pregunta.getValorCorrecto());

    if (pregunta.getEjercicio() != null) {
        dto.setEjercicioId(pregunta.getEjercicio().getId());
    }

    return dto;
}
}