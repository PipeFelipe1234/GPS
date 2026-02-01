package com.practica.backend.service;

import com.practica.backend.dto.LoginRequest;
import com.practica.backend.dto.LoginResponse;
import com.practica.backend.entity.Usuario;
import com.practica.backend.repository.UsuarioRepository;
import com.practica.backend.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public LoginResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository.findByIdentificacion(request.identificacion())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        String token = JwtUtil.generarToken(
                usuario.getIdentificacion(),
                usuario.getRol(),
                usuario.getNombre());

        return new LoginResponse(token);
    }

    public LoginResponse refrescarToken(String authHeader) {
        // Validar que el header sea válido
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token inválido o ausente");
        }

        String token = authHeader.substring(7);

        // Refrescar el token
        String nuevoToken = JwtUtil.refrescarToken(token);

        if (nuevoToken == null) {
            throw new RuntimeException("No se pudo refrescar el token. Por favor, inicia sesión nuevamente.");
        }

        return new LoginResponse(nuevoToken);
    }
}
