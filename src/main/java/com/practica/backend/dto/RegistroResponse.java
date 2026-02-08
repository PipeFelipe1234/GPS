package com.practica.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record RegistroResponse(
                Long id,
                LocalDate fecha,
                LocalTime horaEntrada,
                LocalTime horaSalida,
                Double latitud,
                Double longitud,
                Double precisionMetros,
                Double latitudCheckin,
                Double longitudCheckin,
                Double precisionMetrosCheckin,
                String reporte,
                String picture,
                String identificacion,
                String nombre,
                String foto,
                String telefono,
                Integer horasTrabajadas, // Total de horas trabajadas (entero)
                Boolean enCurso // true si aún no ha marcado salida
) {
}