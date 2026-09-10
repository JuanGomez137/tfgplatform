package com.juan.tfgplatform.controller;

import com.juan.tfgplatform.dto.EjercicioRequestDTO;
import com.juan.tfgplatform.dto.EjercicioResponseDTO;
import com.juan.tfgplatform.model.Ejercicio;
import com.juan.tfgplatform.service.EjercicioService;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioController {

    private final EjercicioService ejercicioService;

    public EjercicioController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    // =============================
    // CREAR
    // =============================

    @PostMapping
    public EjercicioResponseDTO crear(@RequestBody EjercicioRequestDTO dto) {


        Ejercicio ejercicio = ejercicioService.crearEjercicio(
                dto.getTitulo(),
                dto.getDescripcion(),
                dto.getFechaLimite(),
                dto.getProfesorId()
        );

        return convertirADTO(ejercicio);
    }

    // =============================
    // ASIGNAR A GRUPOS
    // =============================

    @PostMapping("/{ejercicioId}/grupos")
    public EjercicioResponseDTO asignarAGrupos(@PathVariable Long ejercicioId,
                                               @RequestBody List<Long> grupoIds) {

        Ejercicio ejercicio = ejercicioService.asignarAGrupos(ejercicioId, grupoIds);
        return convertirADTO(ejercicio);
    }

    // =============================
    // LISTAR
    // =============================

    @GetMapping
    public List<EjercicioResponseDTO> listar() {

        List<Ejercicio> ejercicios = ejercicioService.listarTodos();
        List<EjercicioResponseDTO> respuesta = new ArrayList<>();

        for (Ejercicio ejercicio : ejercicios) {
            respuesta.add(convertirADTO(ejercicio));
        }

        return respuesta;
    }

    // =============================
    // OBTENER POR ID
    // =============================

    @GetMapping("/{id}")
    public EjercicioResponseDTO obtener(@PathVariable Long id) {

        Ejercicio ejercicio = ejercicioService.obtenerPorId(id);
        return convertirADTO(ejercicio);
    }

    // =============================
    // ELIMINAR
    // =============================

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        ejercicioService.eliminarEjercicio(id);
    }

    // =============================
    // MÉTODO PRIVADO DE CONVERSIÓN
    // =============================

    private EjercicioResponseDTO convertirADTO(Ejercicio ejercicio) {

        EjercicioResponseDTO dto = new EjercicioResponseDTO();

        dto.setId(ejercicio.getId());
        dto.setTitulo(ejercicio.getTitulo());
        dto.setDescripcion(ejercicio.getDescripcion());
        dto.setFechaPublicacion(ejercicio.getFechaPublicacion());
        dto.setFechaLimite(ejercicio.getFechaLimite());
        dto.setCreadorId(ejercicio.getCreador().getId());

        Set<Long> gruposIds = new HashSet<>();
        ejercicio.getGrupos().forEach(g -> gruposIds.add(g.getId()));
        dto.setGruposIds(gruposIds);

        return dto;
    }
}