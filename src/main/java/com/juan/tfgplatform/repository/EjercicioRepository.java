package com.juan.tfgplatform.repository;

import com.juan.tfgplatform.model.Ejercicio;
import com.juan.tfgplatform.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {

    List<Ejercicio> findByCreador(Usuario creador);

}