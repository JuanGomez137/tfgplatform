package com.juan.tfgplatform.controller;

import com.juan.tfgplatform.dto.UsuarioRequestDTO;
import com.juan.tfgplatform.dto.UsuarioResponseDTO;
import com.juan.tfgplatform.model.Usuario;
import com.juan.tfgplatform.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
public UsuarioResponseDTO crear(@Valid @RequestBody UsuarioRequestDTO dto) {

    Usuario usuario = new Usuario();
    usuario.setNombre(dto.getNombre());
    usuario.setEmail(dto.getEmail());
    usuario.setPasswordHash(dto.getPassword());
    usuario.setRol(usuarioService.determinarRolPorEmail(dto.getEmail()));

    Usuario guardado = usuarioService.crearUsuario(usuario);

    UsuarioResponseDTO response = new UsuarioResponseDTO();
    response.setId(guardado.getId());
    response.setNombre(guardado.getNombre());
    response.setEmail(guardado.getEmail());
    response.setRol(guardado.getRol().name());
    response.setActivo(guardado.getActivo());

    return response;
}

    @GetMapping
public List<UsuarioResponseDTO> listar() {

    List<Usuario> usuarios = usuarioService.listarTodos();

    List<UsuarioResponseDTO> respuesta = new ArrayList<>();

    for (Usuario usuario : usuarios) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol().name());
        dto.setActivo(usuario.getActivo());

        respuesta.add(dto);
    }

    return respuesta;
}

    @GetMapping("/{id}")
public UsuarioResponseDTO obtener(@PathVariable Long id) {

    Usuario usuario = usuarioService.obtenerPorId(id);

    UsuarioResponseDTO dto = new UsuarioResponseDTO();
    dto.setId(usuario.getId());
    dto.setNombre(usuario.getNombre());
    dto.setEmail(usuario.getEmail());
    dto.setRol(usuario.getRol().name());
    dto.setActivo(usuario.getActivo());

    return dto;
}

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id,
                               @RequestBody Usuario usuario) {
        return usuarioService.actualizarUsuario(id, usuario);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }
}