package com.practica.backend.controller;

import com.practica.backend.dto.UsuarioRequest;
import com.practica.backend.dto.UsuarioResponse;
import com.practica.backend.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // 👤 VER MIS PROPIOS DATOS
    @GetMapping("/mis-datos")
    public ResponseEntity<UsuarioResponse> obtenerMisDatos() {
        String identificacion = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.ok(usuarioService.obtenerUsuarioResponsePorIdentificacion(identificacion));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(
            @RequestBody UsuarioRequest dto) {
        return ResponseEntity.ok(usuarioService.crearUsuario(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequest dto) {

        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarMiUsuario(
            @RequestBody UsuarioRequest dto) {

        String identificacion = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                usuarioService.actualizarPorIdentificacion(identificacion, dto));
    }

}
