package com.juan.tfgplatform.repository;

import com.juan.tfgplatform.model.Reclamacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamacionRepository extends JpaRepository<Reclamacion, Long> {

    List<Reclamacion> findByEntregaId(Long entregaId);

    List<Reclamacion> findByResueltaFalse();

    boolean existsByRespuestaId(Long respuestaId);

    java.util.Optional<Reclamacion> findByRespuestaId(Long respuestaId);

}