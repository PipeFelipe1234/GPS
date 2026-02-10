package com.practica.backend.dto;

/**
 * Request para exportar registros
 * 
 * @param mes     Mes a exportar (1-12)
 * @param anio    Año a exportar
 * @param formato Formato de exportación: "PDF", "EXCEL", "WORD"
 */
public record ExportRequest(
        Integer mes,
        Integer anio,
        String formato) {
}
