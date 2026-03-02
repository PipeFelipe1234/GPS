package com.practica.backend.controller;

import com.practica.backend.dto.ExportRequest;
import com.practica.backend.dto.RegistroFilterRequest;
import com.practica.backend.dto.UsuarioUpdateRequest;
import com.practica.backend.service.ExportService;
import com.practica.backend.service.RegistroService;
import com.practica.backend.service.ScheduledCleanupService;
import com.practica.backend.service.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    private final RegistroService registroService;
    private final UsuarioService usuarioService;
    private final ExportService exportService;
    private final ScheduledCleanupService cleanupService;

    public AdminController(
            RegistroService registroService,
            UsuarioService usuarioService,
            ExportService exportService,
            ScheduledCleanupService cleanupService) {
        this.registroService = registroService;
        this.usuarioService = usuarioService;
        this.exportService = exportService;
        this.cleanupService = cleanupService;
    }

    // 👮 VER TODOS LOS REGISTROS
    @GetMapping("/registros")
    public ResponseEntity<?> todosLosRegistros() {
        return ResponseEntity.ok(registroService.obtenerTodos());
    }

    // 👮 VER TODOS LOS USUARIOS
    @GetMapping("/usuarios")
    public ResponseEntity<?> todosLosUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    // 👮 VER UN USUARIO ESPECÍFICO POR ID
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioResponsePorId(id));
    }

    // � MODIFICAR UN USUARIO POR ID (actualización parcial)
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarUsuarioParcial(id, request));
    }

    // �🔍 FILTRAR REGISTROS POR FECHA, IDENTIFICACIÓN O NOMBRES
    @PostMapping("/registros/filtrar")
    public ResponseEntity<?> filtrarRegistros(@RequestBody RegistroFilterRequest filtro) {
        return ResponseEntity.ok(registroService.filtrarRegistros(filtro));
    }

    // ============================
    // 📤 EXPORTACIÓN DE REGISTROS (ADMIN)
    // ============================

    /**
     * Obtiene información sobre la próxima limpieza automática
     * Incluye advertencia si faltan 4 días hábiles o menos
     */
    @GetMapping("/limpieza/info")
    public ResponseEntity<?> obtenerInfoLimpieza() {
        return ResponseEntity.ok(cleanupService.obtenerInfoLimpieza());
    }

    /**
     * Obtiene los meses disponibles para exportar
     */
    @GetMapping("/exportar/meses")
    public ResponseEntity<?> obtenerMesesDisponibles() {
        return ResponseEntity.ok(cleanupService.obtenerMesesDisponibles());
    }

    /**
     * Exporta TODOS los registros del mes a PDF (todos los empleados)
     */
    @PostMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(@RequestBody ExportRequest request) {
        try {
            LocalDate fechaActual = LocalDate.now(ZONA_COLOMBIA);
            int mes = request.mes() != null ? request.mes() : fechaActual.getMonthValue();
            int anio = request.anio() != null ? request.anio() : fechaActual.getYear();

            byte[] pdfBytes = exportService.exportarPdfAdmin(mes, anio);

            String nombreMes = exportService.getNombreMes(mes);
            String filename = "Registros_Todos_" + nombreMes + "_" + anio + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    /**
     * Exporta TODOS los registros del mes a Excel (todos los empleados)
     */
    @PostMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(@RequestBody ExportRequest request) {
        try {
            LocalDate fechaActual = LocalDate.now(ZONA_COLOMBIA);
            int mes = request.mes() != null ? request.mes() : fechaActual.getMonthValue();
            int anio = request.anio() != null ? request.anio() : fechaActual.getYear();

            byte[] excelBytes = exportService.exportarExcelAdmin(mes, anio);

            String nombreMes = exportService.getNombreMes(mes);
            String filename = "Registros_Todos_" + nombreMes + "_" + anio + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage());
        }
    }

    /**
     * Fuerza la eliminación de registros de un mes específico (SOLO ADMIN)
     * Útil para pruebas o limpieza manual
     */
    @DeleteMapping("/registros/limpiar/{mes}/{anio}")
    public ResponseEntity<?> forzarLimpieza(@PathVariable int mes, @PathVariable int anio) {
        int eliminados = cleanupService.forzarEliminacionMes(mes, anio);
        String nombreMes = exportService.getNombreMes(mes);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Se eliminaron " + eliminados + " registros del mes de " + nombreMes + " " + anio,
                "eliminados", eliminados,
                "mes", nombreMes,
                "anio", anio));
    }
}
