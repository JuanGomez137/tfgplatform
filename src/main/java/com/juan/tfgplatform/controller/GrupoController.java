package com.juan.tfgplatform.controller;

import com.juan.tfgplatform.dto.EntregaResponseDTO;
import com.juan.tfgplatform.dto.GrupoRequestDTO;
import com.juan.tfgplatform.dto.GrupoResponseDTO;
import com.juan.tfgplatform.dto.UsuarioResponseDTO;
import com.juan.tfgplatform.model.Entrega;
import com.juan.tfgplatform.model.Grupo;
import com.juan.tfgplatform.model.Usuario;
import com.juan.tfgplatform.service.EntregaService;
import com.juan.tfgplatform.service.GrupoService;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    private final EntregaService entregaService;
    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService, EntregaService entregaService) {
        this.grupoService = grupoService;
        this.entregaService = entregaService;
    }

    @PostMapping
    public GrupoResponseDTO crear(@RequestBody GrupoRequestDTO dto) {
        Grupo grupo = grupoService.crearGrupo(
                dto.getNombre(),
                dto.getDescripcion(),
                dto.getProfesorId()
        );
        return toGrupoDTO(grupo);
    }

    @PostMapping("/{grupoId}/alumnos/{alumnoId}")
    public GrupoResponseDTO agregarAlumno(@PathVariable Long grupoId,
                                          @PathVariable Long alumnoId) {
        Grupo grupo = grupoService.agregarAlumno(grupoId, alumnoId);
        return toGrupoDTO(grupo);
    }

    @GetMapping
    public List<GrupoResponseDTO> listar() {
        List<GrupoResponseDTO> respuesta = new ArrayList<>();
        for (Grupo grupo : grupoService.listarTodos()) {
            respuesta.add(toGrupoDTO(grupo));
        }
        return respuesta;
    }

    @GetMapping("/{id}")
    public GrupoResponseDTO obtener(@PathVariable Long id) {
        return toGrupoDTO(grupoService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        grupoService.eliminarGrupo(id);
    }

    @DeleteMapping("/{grupoId}/alumnos/{alumnoId}")
    public void quitarAlumno(@PathVariable Long grupoId, @PathVariable Long alumnoId) {
        grupoService.quitarAlumno(grupoId, alumnoId);
    }

    @PutMapping("/{id}")
    public GrupoResponseDTO editar(@PathVariable Long id,
                                   @RequestParam String nombre,
                                   @RequestParam String descripcion) {
        return toGrupoDTO(grupoService.editarGrupo(id, nombre, descripcion));
    }

    
    @GetMapping("/{id}/alumnos")
    public Set<UsuarioResponseDTO> verAlumnos(@PathVariable Long id) {
        Set<UsuarioResponseDTO> resultado = new HashSet<>();
        for (Usuario alumno : grupoService.obtenerPorId(id).getAlumnos()) {
            resultado.add(toUsuarioDTO(alumno));
        }
        return resultado;
    }

    @GetMapping("/{grupoId}/notas")
    public List<EntregaResponseDTO> verNotas(@PathVariable Long grupoId) {
        List<EntregaResponseDTO> resultado = new ArrayList<>();
        for (Entrega e : entregaService.obtenerNotasPorGrupo(grupoId)) {
            resultado.add(toEntregaDTO(e));
        }
        return resultado;
    }

    @PostMapping("/crear-con-emails")
    public GrupoResponseDTO crearConEmails(@RequestParam String nombre,
                                           @RequestParam String descripcion,
                                           @RequestParam Long profesorId,
                                           @RequestParam List<String> emails) {
        return toGrupoDTO(grupoService.crearGrupoConEmails(nombre, descripcion, profesorId, emails));
    }

    // =============================
    // CONVERSIÓN A DTOs
    // =============================

    private GrupoResponseDTO toGrupoDTO(Grupo grupo) {
        GrupoResponseDTO dto = new GrupoResponseDTO();
        dto.setId(grupo.getId());
        dto.setNombre(grupo.getNombre());
        dto.setDescripcion(grupo.getDescripcion());
        dto.setProfesorId(grupo.getProfesor().getId());
        Set<Long> alumnosIds = new HashSet<>();
        grupo.getAlumnos().forEach(a -> alumnosIds.add(a.getId()));
        dto.setAlumnosIds(alumnosIds);
        Set<Long> ejerciciosIds = new HashSet<>();
        grupo.getEjercicios().forEach(e -> ejerciciosIds.add(e.getId()));
        dto.setEjerciciosIds(ejerciciosIds);
        return dto;
    }

    private UsuarioResponseDTO toUsuarioDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol().name());
        dto.setActivo(usuario.getActivo());
        return dto;
    }

    private EntregaResponseDTO toEntregaDTO(Entrega entrega) {
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
