package com.practica.backend.dto;

/**
 * Response con información sobre la próxima limpieza automática de registros
 * 
 * @param hayAdvertencia    true si hay una advertencia activa sobre eliminación
 *                          próxima
 * @param mensaje           Mensaje descriptivo sobre la eliminación
 * @param mesAEliminar      Nombre del mes que será eliminado
 * @param anioAEliminar     Año del mes que será eliminado
 * @param diasRestantes     Días hábiles restantes antes de la eliminación
 * @param fechaEliminacion  Fecha en que se ejecutará la eliminación (formato
 *                          ISO)
 * @param cantidadRegistros Cantidad de registros que serán eliminados
 * @param puedeExportar     true si aún puede exportar los registros
 */
public record CleanupInfoResponse(
        boolean hayAdvertencia,
        String mensaje,
        String mesAEliminar,
        Integer anioAEliminar,
        Integer diasRestantes,
        String fechaEliminacion,
        Long cantidadRegistros,
        boolean puedeExportar) {
}
