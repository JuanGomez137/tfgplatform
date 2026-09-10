package com.juan.tfgplatform.service;

import com.juan.tfgplatform.model.Usuario;
import com.juan.tfgplatform.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    //Seguridad para la contraseña
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario crearUsuario(Usuario usuario) {

        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {
            throw new RuntimeException("La contraseña no puede estar vacia");
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()){
            throw new RuntimeException("Email ya registrado");
        }
        
        usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
        usuario.setFechaCreacion(LocalDateTime.now());
        // activo is left as-is so the caller can set false for email-confirmation flow
        // (model default is true, so if caller hasn't explicitly set it false, it stays true)
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Usuario actualizarUsuario(Long id, Usuario datos) {
        Usuario usuario = obtenerPorId(id);
        usuario.setNombre(datos.getNombre());
        usuario.setEmail(datos.getEmail());
        // El rol NO se actualiza desde aquí para evitar escalada de privilegios
        usuario.setActivo(datos.getActivo());
        return usuarioRepository.save(usuario);
    }

    public void cambiarPassword(Long usuarioId, String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.isBlank() || nuevaPassword.length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }
        Usuario usuario = obtenerPorId(usuarioId);
        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario login(String email, String password){
        Usuario usuario =usuarioRepository.findByEmail(email).orElseThrow( () -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(password, usuario.getPasswordHash())){
            throw new RuntimeException("Contraseña incorrecta");
        }
        return usuario;
    }


    public Usuario.Rol determinarRolPorEmail(String email) {

    if (email == null) {
        throw new IllegalArgumentException("El email es obligatorio");
    }

    String emailLower = email.toLowerCase().trim();

    if (emailLower.endsWith("@alumnos.upm.es")) {
        return Usuario.Rol.ALUMNO;
    }

    if (emailLower.endsWith("@upm.es")) {
        return Usuario.Rol.PROFESOR;
    }

    throw new IllegalArgumentException(
            "Solo se permiten correos institucionales de la UPM");
}
}