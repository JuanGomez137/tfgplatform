package com.juan.tfgplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:noreply@upm.es}")
    private String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarEmailRecuperacion(String email, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(email);
        msg.setSubject("Recuperación de contraseña — Plataforma TFG UPM");
        msg.setText(
            "Hola,\n\n" +
            "Has solicitado restablecer tu contraseña en la Plataforma TFG UPM.\n\n" +
            "Haz clic en el siguiente enlace (válido durante 1 hora):\n\n" +
            baseUrl + "/reset-contrasena?token=" + token + "\n\n" +
            "Si no has solicitado este cambio, ignora este correo.\n\n" +
            "Un saludo,\nPlataforma TFG UPM"
        );
        mailSender.send(msg);
    }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public void enviarEmailConfirmacion(String email, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(email);
        msg.setSubject("Confirma tu cuenta — Plataforma TFG UPM");
        msg.setText(
            "Hola,\n\n" +
            "Gracias por registrarte en la Plataforma TFG UPM.\n\n" +
            "Para activar tu cuenta haz clic en el siguiente enlace:\n\n" +
            baseUrl + "/confirmar-email?token=" + token + "\n\n" +
            "El enlace caduca en 24 horas.\n\n" +
            "Un saludo,\nPlataforma TFG UPM"
        );
        mailSender.send(msg);
    }

    @Override
    public void enviarEmailNotificacion(String email, String nombreAlumno,
                                        String tituloEjercicio, BigDecimal nota) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(email);
        msg.setSubject("Nueva nota disponible — " + tituloEjercicio);
        msg.setText(
            "Hola " + nombreAlumno + ",\n\n" +
            "Tu nota para el ejercicio \"" + tituloEjercicio + "\" ya está disponible.\n" +
            "Nota obtenida: " + nota + "\n\n" +
            "Inicia sesión en la plataforma para consultarla.\n\n" +
            "Un saludo,\nPlataforma TFG UPM"
        );
        mailSender.send(msg);
    }
}
