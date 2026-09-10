package com.juan.tfgplatform.repository;

import com.juan.tfgplatform.model.Grupo;
import com.juan.tfgplatform.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    List<Grupo> findByProfesor(Usuario profesor);

}