package com.practica.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MarcarSalidaRequest(
                @JsonProperty("latitudCheckout") Double latitud,
                @JsonProperty("longitudCheckout") Double longitud,
                @JsonProperty("precisionMetrosCheckout") Double precisionMetros,
                String reporte,
                String picture) {
}
