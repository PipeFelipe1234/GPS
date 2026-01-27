package com.practica.backend.dto;

public record UsuarioResponse(
                Long id,
                String identificacion,
                String nombre,
                String email,
                String rol,
                String foto,
                Integer telefono) {
}
