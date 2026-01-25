package com.practica.backend.dto;

public record MarcarSalidaRequest(
                Double latitud,
                Double longitud,
                Double precisionMetros,
                String reporte,
                String picture) {
}
