package com.practica.backend.controller;

import com.practica.backend.dto.MarcarEntradaRequest;
import com.practica.backend.dto.MarcarSalidaRequest;
import com.practica.backend.dto.RegistroResponse;
import com.practica.backend.entity.Usuario;
import com.practica.backend.service.RegistroService;
import com.practica.backend.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registros")
@CrossOrigin(origins = "*")
public class RegistroController {

        private final RegistroService registroService;
        private final UsuarioService usuarioService;

        public RegistroController(
                        RegistroService registroService,
                        UsuarioService usuarioService) {
                this.registroService = registroService;
                this.usuarioService = usuarioService;
        }

        @PostMapping("/entrada")
        public ResponseEntity<RegistroResponse> marcarEntrada(@RequestBody MarcarEntradaRequest request) {

                String identificacion = SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName();

                Usuario usuario = usuarioService.obtenerPorIdentificacion(identificacion);

                return ResponseEntity.ok(registroService.marcarEntrada(usuario, request));
        }

        @PostMapping("/salida")
        public ResponseEntity<RegistroResponse> marcarSalida(
                        @RequestBody MarcarSalidaRequest request) {
                String identificacion = SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName();

                Usuario usuario = usuarioService.obtenerPorIdentificacion(identificacion);

                return ResponseEntity.ok(
                                registroService.marcarSalida(usuario, request));
        }

        @GetMapping("/mis-registros")
        public ResponseEntity<?> misRegistros() {

                String identificacion = SecurityContextHolder.getContext().getAuthentication().getName();

                Usuario usuario = usuarioService.obtenerPorIdentificacion(identificacion);

                return ResponseEntity.ok(
                                registroService.obtenerMisRegistros(usuario));
        }

}
