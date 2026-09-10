package com.juan.tfgplatform.service;

import com.juan.tfgplatform.model.Ejercicio;
import com.juan.tfgplatform.model.Pregunta;
import com.juan.tfgplatform.repository.EjercicioRepository;
import com.juan.tfgplatform.repository.PreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final EjercicioRepository ejercicioRepository;

    public PreguntaService(PreguntaRepository preguntaRepository,
                           EjercicioRepository ejercicioRepository) {
        this.preguntaRepository = preguntaRepository;
        this.ejercicioRepository = ejercicioRepository;
    }

    public Pregunta crearPregunta(Long ejercicioId, Pregunta pregunta) {
        Ejercicio ejercicio = ejercicioRepository.findById(ejercicioId)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));

        pregunta.setEjercicio(ejercicio);

        return preguntaRepository.save(pregunta);
    }

    public List<Pregunta> listarTodas() {
        return preguntaRepository.findAll();
    }

    public List<Pregunta> listarPorEjercicio(Long ejercicioId){
        return preguntaRepository.findByEjercicioId(ejercicioId);
    }
}