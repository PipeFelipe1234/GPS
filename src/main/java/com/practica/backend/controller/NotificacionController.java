package com.practica.backend.controller;

import com.practica.backend.dto.TokenDispositivoRequest;
import com.practica.backend.security.JwtUtil;
import com.practica.backend.service.NotificacionService;
import com.practica.backend.service.UsuarioService;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    public NotificacionController(
            NotificacionService notificacionService,
            UsuarioService usuarioService) {
        this.notificacionService = notificacionService;
        this.usuarioService = usuarioService;
    }

    // 📱 REGISTRAR TOKEN DE DISPOSITIVO
    @PostMapping("/registrar-token")
    public ResponseEntity<?> registrarToken(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TokenDispositivoRequest request) {
        try {
            // Obtener identificación del usuario desde el JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Token no válido");
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.extraerClaimsIgnorandoExpiracion(token);
            String identificacion = claims.getSubject();

            // Obtener usuario
            var usuario = usuarioService.obtenerPorIdentificacion(identificacion);
            if (usuario == null) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            // Registrar token
            notificacionService.registrarTokenDispositivo(
                    usuario,
                    request.getToken(),
                    request.getTipoDispositivo(),
                    request.getMarca(),
                    request.getModelo());

            return ResponseEntity.ok(new ResponseDto("✅ Token registrado correctamente", true));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDto("❌ Error al registrar token: " + e.getMessage(), false));
        }
    }

    // ❌ DESACTIVAR TOKEN
    @PostMapping("/desactivar-token")
    public ResponseEntity<?> desactivarToken(@RequestBody TokenDesactivarRequest request) {
        try {
            notificacionService.desactivarToken(request.getToken());
            return ResponseEntity.ok(new ResponseDto("✅ Token desactivado correctamente", true));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDto("❌ Error al desactivar token: " + e.getMessage(), false));
        }
    }

    // 📲 DTO SIMPLES
    public static class ResponseDto {
        public String mensaje;
        public Boolean exito;

        public ResponseDto(String mensaje, Boolean exito) {
            this.mensaje = mensaje;
            this.exito = exito;
        }
    }

    public static class TokenDesactivarRequest {
        public String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
