package com.juan.tfgplatform.service;

import java.math.BigDecimal;

public interface EmailService {

    
    void enviarEmailRecuperacion(String email, String token);

    
    void enviarEmailNotificacion(String email, String nombreAlumno,
                                 String tituloEjercicio, BigDecimal nota);

    
    void enviarEmailConfirmacion(String email, String token);

   
    default boolean isEnabled() { return false; }
}
