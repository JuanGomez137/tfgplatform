package com.juan.tfgplatform.service;

import com.juan.tfgplatform.model.Ejercicio;
import com.juan.tfgplatform.model.Grupo;
import com.juan.tfgplatform.model.Usuario;
import com.juan.tfgplatform.repository.GrupoRepository;
import com.juan.tfgplatform.repository.UsuarioRepository;
import com.juan.tfgplatform.repository.EjercicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EjercicioRepository ejercicioRepository;


    public GrupoService(GrupoRepository grupoRepository,
                        UsuarioRepository usuarioRepository,
                    EjercicioRepository ejercicioRepository) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ejercicioRepository = ejercicioRepository;
    }

    public Grupo crearGrupo(String nombre, String descripcion, Long profesorId) {
        Usuario profesor = usuarioRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        if (profesor.getRol() != Usuario.Rol.PROFESOR) {
            throw new RuntimeException("El usuario no es profesor");
        }

        Grupo grupo = new Grupo();
        grupo.setNombre(nombre);
        grupo.setDescripcion(descripcion);
        grupo.setProfesor(profesor);

        return grupoRepository.save(grupo);
    }

    public Grupo agregarAlumno(Long grupoId, Long alumnoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        Usuario alumno = usuarioRepository.findById(alumnoId)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        if (alumno.getRol() != Usuario.Rol.ALUMNO) {
            throw new RuntimeException("El usuario no es alumno");
        }

        grupo.agregarAlumno(alumno);

        return grupoRepository.save(grupo);
    }

    public List<Grupo> listarTodos() {
        return grupoRepository.findAll();
    }

    public Grupo obtenerPorId(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
    }

    public void quitarAlumno(Long grupoId, Long alumnoId){
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        Usuario alumno = usuarioRepository.findById(alumnoId).orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
        
        grupo.quitarAlumno(alumno);
        grupoRepository.save(grupo);
   
    }

    public Grupo editarGrupo(Long id, String nombre, String descripcion){
        Grupo grupo = grupoRepository.findById(id).orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        grupo.setNombre(nombre);
        grupo.setDescripcion(descripcion);

        return grupoRepository.save(grupo);
    }

    public Grupo crearGrupoConEmails(String nombre,
                                 String descripcion,
                                 Long profesorId,
                                 List<String> emails) {

    Usuario profesor = usuarioRepository.findById(profesorId)
            .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

    Grupo grupo = new Grupo();
    grupo.setNombre(nombre);
    grupo.setDescripcion(descripcion);
    grupo.setProfesor(profesor);

    for (String email : emails) {

        Usuario alumno = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado: " + email));

        grupo.agregarAlumno(alumno);
    }

    return grupoRepository.save(grupo);
}

public void asignarEjercicio(Long grupoId, Long ejercicioId){
    Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

    Ejercicio ejercicio = ejercicioRepository.findById(ejercicioId).orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));

    grupo.agregarEjercicio(ejercicio);
    grupoRepository.save(grupo);
}


    public void eliminarGrupo(Long id) {
        grupoRepository.deleteById(id);
    }
}