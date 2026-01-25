package com.practica.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record RegistroResponse(
        Long id,
        LocalDate fecha,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        Double latitudCheckin,
        Double longitudCheckin,
        Double precisionMetrosCheckin,
        Double latitudCheckout,
        Double longitudCheckout,
        Double precisionMetrosCheckout,
        String reporte,
        String picture,
        String identificacion) {
}