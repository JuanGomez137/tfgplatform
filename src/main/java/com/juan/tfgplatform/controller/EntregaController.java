package com.juan.tfgplatform.controller;

import com.juan.tfgplatform.dto.EntregaRequestDTO;
import com.juan.tfgplatform.dto.EntregaResponseDTO;
import com.juan.tfgplatform.model.Entrega;
import com.juan.tfgplatform.service.EntregaService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @PostMapping
    public EntregaResponseDTO crear(@RequestBody EntregaRequestDTO dto) {
        Entrega entrega = entregaService.crearEntrega(
                dto.getAlumnoId(),
                dto.getEjercicioId()
        );
        return toDTO(entrega);
    }

    @GetMapping
    public List<EntregaResponseDTO> listar() {
        List<EntregaResponseDTO> respuesta = new ArrayList<>();
        for (Entrega entrega : entregaService.listarTodas()) {
            respuesta.add(toDTO(entrega));
        }
        return respuesta;
    }

    @PostMapping("/{id}/enviar")
    public EntregaResponseDTO enviar(@PathVariable Long id) {
        Entrega entrega = entregaService.enviarEntrega(id);
        return toDTO(entrega);
    }

    @PatchMapping("/{id}/nota")
    public EntregaResponseDTO cambiarNota(@PathVariable Long id, @RequestParam BigDecimal nota) {
        Entrega entrega = entregaService.cambiarNota(id, nota);
        return toDTO(entrega);
    }

    private EntregaResponseDTO toDTO(Entrega entrega) {
        EntregaResponseDTO dto = new EntregaResponseDTO();
        dto.setId(entrega.getId());
        dto.setFechaEntrega(entrega.getFechaEntrega());
        dto.setNotaTotal(entrega.getNotaTotal());
        dto.setEstado(entrega.getEstado().name());
        dto.setAlumnoId(entrega.getAlumno().getId());
        dto.setEjercicioId(entrega.getEjercicio().getId());
        return dto;
    }
}
