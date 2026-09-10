package com.juan.tfgplatform.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Fallback email
 */
@Service
@ConditionalOnMissingBean(EmailServiceImpl.class)
public class NoOpEmailService implements EmailService {

    @Override
    public void enviarEmailRecuperacion(String email, String token) {
        System.out.println("[EMAIL no configurado] Recuperación para: " + email + " | token: " + token);
    }

    @Override
    public void enviarEmailConfirmacion(String email, String token) {
        System.out.println("[EMAIL no configurado] Confirmación para: " + email +
                " | URL: /confirmar-email?token=" + token);
    }

    @Override
    public void enviarEmailNotificacion(String email, String nombreAlumno,
                                        String tituloEjercicio, BigDecimal nota) {
        System.out.println("[EMAIL no configurado] Notificación para: " + email +
                " | Ejercicio: " + tituloEjercicio + " | Nota: " + nota);
    }
}
