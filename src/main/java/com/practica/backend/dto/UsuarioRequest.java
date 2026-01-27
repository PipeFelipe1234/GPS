package com.practica.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRequest(
                @NotBlank String identificacion,
                @NotBlank String nombre,
                @Email String email,
                @NotBlank String rol,
                String foto,
                Integer telefono) {
}