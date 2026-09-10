package com.juan.tfgplatform.repository;

import com.juan.tfgplatform.model.Entrega;
import com.juan.tfgplatform.model.Ejercicio;
import com.juan.tfgplatform.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.List;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    Optional<Entrega> findByAlumnoAndEjercicio(Usuario alumno, Ejercicio ejercicio);

    List<Entrega> findByEjercicio(Ejercicio ejercicio);

    List<Entrega> findByAlumno(Usuario alumno);

    List<Entrega> findByAlumnoId(Long alumnoId);

    List<Entrega> findByEjercicio_Grupos_Id(Long grupoId);

}