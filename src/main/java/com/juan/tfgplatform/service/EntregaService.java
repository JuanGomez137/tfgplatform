package com.juan.tfgplatform.service;

import com.juan.tfgplatform.model.Entrega;
import com.juan.tfgplatform.model.Respuesta;
import com.juan.tfgplatform.model.Ejercicio;
import com.juan.tfgplatform.model.Usuario;
import com.juan.tfgplatform.repository.EntregaRepository;
import com.juan.tfgplatform.repository.RespuestaRepository;
import com.juan.tfgplatform.repository.EjercicioRepository;
import com.juan.tfgplatform.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EjercicioRepository ejercicioRepository;
    private final RespuestaRepository respuestaRepository;
    private final EmailService emailService;

    public EntregaService(EntregaRepository entregaRepository,
                          UsuarioRepository usuarioRepository,
                          EjercicioRepository ejercicioRepository,
                          RespuestaRepository respuestaRepository,
                          EmailService emailService) {
        this.entregaRepository = entregaRepository;
        this.usuarioRepository = usuarioRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.respuestaRepository = respuestaRepository;
        this.emailService = emailService;
    }

    public Entrega crearEntrega(Long alumnoId, Long ejercicioId) {

        Usuario alumno = usuarioRepository.findById(alumnoId)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        Ejercicio ejercicio = ejercicioRepository.findById(ejercicioId)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));

        Entrega entrega = new Entrega();
        entrega.setAlumno(alumno);
        entrega.setEjercicio(ejercicio);
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setEstado(Entrega.EstadoEntrega.EN_PROGRESO);

        return entregaRepository.save(entrega);
    }

    public Entrega enviarEntrega(Long entregaId){
        Entrega entrega = entregaRepository.findById(entregaId).orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
        if (entrega.getEstado() != Entrega.EstadoEntrega.EN_PROGRESO) {
            throw new RuntimeException("La entrega ya ha sido enviada");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Respuesta r : entrega.getRespuestas()){
            if (r.getPuntuacionObtenida() != null) {
                total = total.add(r.getPuntuacionObtenida());
            }
        }
        entrega.setEstado(Entrega.EstadoEntrega.CORREGIDO_AUTO);
        entrega.setNotaTotal(total);

        return entregaRepository.save(entrega);
    }
    
    public List<Entrega> obtenerNotasPorGrupo(Long grupoId){
        return entregaRepository.findByEjercicio_Grupos_Id(grupoId);
    }

    public Entrega cambiarNota(Long entregaId, BigDecimal nuevaNota){
        Entrega entrega = entregaRepository.findById(entregaId).orElseThrow(() -> new RuntimeException("Entrega no encontrada"));

        entrega.setNotaTotal(nuevaNota);
        entrega.setEstado(Entrega.EstadoEntrega.CORREGIDO_MANUAL); 
        return entregaRepository.save(entrega);
    }

    public void calcularNotaEntrega(Long entregaId){
        Entrega entrega = entregaRepository.findById(entregaId).orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
        BigDecimal total = BigDecimal.ZERO;

        List<Respuesta> respuestas = respuestaRepository.findByEntregaId(entregaId);

        for(Respuesta respuesta : respuestas){
            if(respuesta.getPuntuacionObtenida() != null){
                total = total.add(respuesta.getPuntuacionObtenida());
            }
        }

        entrega.setNotaTotal(total);
        entrega.setEstado(Entrega.EstadoEntrega.CORREGIDO_AUTO);
        entrega.setVisibleAlumno(false);

        entregaRepository.save(entrega);

    }

    public void publicarNota(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
        entrega.setVisibleAlumno(true);
        entregaRepository.save(entrega);
        try {
            emailService.enviarEmailNotificacion(
                    entrega.getAlumno().getEmail(),
                    entrega.getAlumno().getNombre(),
                    entrega.getEjercicio().getTitulo(),
                    entrega.getNotaTotal());
        } catch (Exception e) {
            System.err.println("[EntregaService] Error al enviar notificación: " + e.getMessage());
        }
    }

    public int publicarNotasPorGrupo(Long grupoId) {
        List<Entrega> entregas = entregaRepository.findByEjercicio_Grupos_Id(grupoId);
        int publicadas = 0;
        for (Entrega entrega : entregas) {
            boolean yaPublicada = entrega.getVisibleAlumno();
            boolean estaCorregida = entrega.getEstado() == Entrega.EstadoEntrega.CORREGIDO_AUTO
                    || entrega.getEstado() == Entrega.EstadoEntrega.CORREGIDO_MANUAL;
            if (!yaPublicada && estaCorregida) {
                entrega.setVisibleAlumno(true);
                entregaRepository.save(entrega);
                publicadas++;
                try {
                    emailService.enviarEmailNotificacion(
                            entrega.getAlumno().getEmail(),
                            entrega.getAlumno().getNombre(),
                            entrega.getEjercicio().getTitulo(),
                            entrega.getNotaTotal());
                } catch (Exception e) {
                    System.err.println("[EntregaService] Error al enviar notificación: " + e.getMessage());
                }
            }
        }
        return publicadas;
    }


    public List<Entrega> listarPorAlumno(Long alumnoId){
        return entregaRepository.findByAlumnoId(alumnoId);
    }

    public List<Entrega> listarTodas(){
        return entregaRepository.findAll();
    }
}