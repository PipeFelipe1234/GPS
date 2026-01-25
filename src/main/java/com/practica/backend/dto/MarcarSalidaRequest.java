package com.practica.backend.dto;

public record MarcarSalidaRequest(
                Double latitudCheckout,
                Double longitudCheckout,
                Double precisionMetrosCheckout,
                String reporte,
                String picture) {
}
