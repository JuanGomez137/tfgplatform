package com.juan.tfgplatform.service;

import com.juan.tfgplatform.model.Ejercicio;
import com.juan.tfgplatform.model.Grupo;
import com.juan.tfgplatform.model.Usuario;
import com.juan.tfgplatform.repository.EjercicioRepository;
import com.juan.tfgplatform.repository.GrupoRepository;
import com.juan.tfgplatform.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EjercicioService {

    private final EjercicioRepository ejercicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;

    public EjercicioService(EjercicioRepository ejercicioRepository,
                            UsuarioRepository usuarioRepository,
                            GrupoRepository grupoRepository) {
        this.ejercicioRepository = ejercicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
    }

    public Ejercicio crearEjercicio(String titulo,
                                    String descripcion,
                                    LocalDateTime fechaLimite,
                                    Long profesorId) {

        Usuario profesor = usuarioRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        if (profesor.getRol() != Usuario.Rol.PROFESOR) {
            throw new RuntimeException("El usuario no es profesor");
        }

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setTitulo(titulo);
        ejercicio.setDescripcion(descripcion);
        ejercicio.setFechaPublicacion(LocalDateTime.now());
        ejercicio.setFechaLimite(fechaLimite);
        ejercicio.setCreador(profesor);

        return ejercicioRepository.save(ejercicio);
    }

    public Ejercicio asignarAGrupos(Long ejercicioId, List<Long> grupoIds) {
        Ejercicio ejercicio = ejercicioRepository.findById(ejercicioId)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));

        // Desasignar de todos los grupos actuales primero
        for (Grupo grupo : new ArrayList<>(ejercicio.getGrupos())) {
            grupo.getEjercicios().remove(ejercicio);
        }
        ejercicio.getGrupos().clear();
        ejercicioRepository.save(ejercicio);

        // Asignar a los grupos seleccionados
        for (Long grupoId : grupoIds) {
            Grupo grupo = grupoRepository.findById(grupoId)
                    .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
            grupo.agregarEjercicio(ejercicio);
        }

        return ejercicioRepository.save(ejercicio);
    }

    public Ejercicio editarEjercicio(Long id, String titulo, String descripcion,
                                     LocalDateTime fechaPublicacion, LocalDateTime fechaLimite) {
        Ejercicio ejercicio = ejercicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));
        if (titulo != null && !titulo.isBlank()) ejercicio.setTitulo(titulo.trim());
        ejercicio.setDescripcion(descripcion != null ? descripcion.trim() : null);
        ejercicio.setFechaPublicacion(fechaPublicacion);
        ejercicio.setFechaLimite(fechaLimite);
        return ejercicioRepository.save(ejercicio);
    }

    public Ejercicio obtenerPorId(Long id) {
        return ejercicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));
    }

    public List<Ejercicio> listarTodos() {
        return ejercicioRepository.findAll();
    }

    public void eliminarEjercicio(Long id) {
        ejercicioRepository.deleteById(id);
    }
}