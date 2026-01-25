package com.practica.backend.dto;

public record MarcarEntradaRequest(
        Double latitudCheckin,
        Double longitudCheckin,
        Double precisionMetrosCheckin) {
}
