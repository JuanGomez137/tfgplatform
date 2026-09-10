package com.juan.tfgplatform.controller;

import com.juan.tfgplatform.dto.LoginRequestDTO;
import com.juan.tfgplatform.dto.LoginResponseDTO;
import com.juan.tfgplatform.model.Usuario;
import com.juan.tfgplatform.service.UsuarioService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dto) {

        Usuario usuario = usuarioService.login(dto.getEmail(), dto.getPassword());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol().name());

        
        return response;
    }
    


}
