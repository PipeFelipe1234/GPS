package com.practica.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MarcarSalidaRequest(
                @JsonProperty("latitud") Double latitudCheckout,
                @JsonProperty("longitud") Double longitudCheckout,
                @JsonProperty("precisionMetros") Double precisionMetrosCheckout,
                String reporte,
                String picture) {
}
