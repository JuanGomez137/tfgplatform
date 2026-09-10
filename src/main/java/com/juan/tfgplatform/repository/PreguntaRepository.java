package com.juan.tfgplatform.repository;

import com.juan.tfgplatform.model.Pregunta;
import com.juan.tfgplatform.model.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findByEjercicio(Ejercicio ejercicio);

    List<Pregunta> findByEjercicioId(Long ejercicioId);

}