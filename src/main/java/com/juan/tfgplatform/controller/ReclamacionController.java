package com.juan.tfgplatform.controller;

import com.juan.tfgplatform.dto.ReclamacionRequestDTO;
import com.juan.tfgplatform.dto.ReclamacionResponseDTO;
import com.juan.tfgplatform.model.Reclamacion;
import com.juan.tfgplatform.service.ReclamacionService;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reclamaciones")
public class ReclamacionController {

    private final ReclamacionService reclamacionService;

    public ReclamacionController(ReclamacionService reclamacionService) {
        this.reclamacionService = reclamacionService;
    }

    @PostMapping
    public ReclamacionResponseDTO crear(@RequestBody ReclamacionRequestDTO dto) {
        Reclamacion reclamacion = reclamacionService.crearReclamacion(
                dto.getEntregaId(),
                dto.getRespuestaId(),
                dto.getComentario()
        );
        return toDTO(reclamacion);
    }

    @GetMapping("/entrega/{entregaId}")
    public List<ReclamacionResponseDTO> listarPorEntrega(@PathVariable Long entregaId) {
        return reclamacionService.obtenerPorEntrega(entregaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/pendientes")
    public List<ReclamacionResponseDTO> pendientes() {
        return reclamacionService.obtenerPendientes()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @PatchMapping("/{id}/resolver")
    public ReclamacionResponseDTO resolver(@PathVariable Long id,
                                           @RequestParam BigDecimal nuevaNota,
                                           @RequestParam(required = false) String comentarioProfesor) {
        Reclamacion reclamacion = reclamacionService.resolverReclamacion(id, nuevaNota, comentarioProfesor);
        return toDTO(reclamacion);
    }

    private ReclamacionResponseDTO toDTO(Reclamacion reclamacion) {
        ReclamacionResponseDTO dto = new ReclamacionResponseDTO();
        dto.setId(reclamacion.getId());
        dto.setComentario(reclamacion.getComentario());
        dto.setComentarioProfesor(reclamacion.getComentarioProfesor());
        dto.setFechaCreacion(reclamacion.getFechaCreacion());
        dto.setResuelta(reclamacion.getResuelta());
        dto.setEntregaId(reclamacion.getEntrega().getId());
        dto.setRespuestaId(reclamacion.getRespuesta().getId());
        return dto;
    }
}
