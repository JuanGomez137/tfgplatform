package com.juan.tfgplatform.repository;

import com.juan.tfgplatform.model.Respuesta;
import com.juan.tfgplatform.model.Entrega;
import com.juan.tfgplatform.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    Optional<Respuesta> findByEntregaAndPregunta(Entrega entrega, Pregunta pregunta);

    List<Respuesta> findByEntrega(Entrega entrega);

        List<Respuesta> findByEntregaId(Long entregaId);

}