package com.practica.backend.controller;

import com.practica.backend.dto.ExportRequest;
import com.practica.backend.dto.MarcarEntradaRequest;
import com.practica.backend.dto.MarcarSalidaRequest;
import com.practica.backend.dto.RegistroResponse;
import com.practica.backend.entity.Usuario;
import com.practica.backend.service.ExportService;
import com.practica.backend.service.RegistroService;
import com.practica.backend.service.ScheduledCleanupService;
import com.practica.backend.service.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/registros")
@CrossOrigin(origins = "*")
public class RegistroController {

        private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

        private final RegistroService registroService;
        private final UsuarioService usuarioService;
        private final ExportService exportService;
        private final ScheduledCleanupService cleanupService;

        public RegistroController(
                        RegistroService registroService,
                        UsuarioService usuarioService,
                        ExportService exportService,
                        ScheduledCleanupService cleanupService) {
                this.registroService = registroService;
                this.usuarioService = usuarioService;
                this.exportService = exportService;
                this.cleanupService = cleanupService;
        }

        @PostMapping("/entrada")
        public ResponseEntity<RegistroResponse> marcarEntrada(@RequestBody MarcarEntradaRequest request) {

                String identificacion = SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName();

                Usuario usuario = usuarioService.obtenerPorIdentificacion(identificacion);

                return ResponseEntity.ok(registroService.marcarEntrada(usuario, request));
        }

        @PostMapping("/salida")
        public ResponseEntity<RegistroResponse> marcarSalida(
                        @RequestBody MarcarSalidaRequest request) {
                String identificacion = SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName();

                Usuario usuario = usuarioService.obtenerPorIdentificacion(identificacion);

                return ResponseEntity.ok(
                                registroService.marcarSalida(usuario, request));
        }

        @GetMapping("/mis-registros")
        public ResponseEntity<?> misRegistros() {

                String identificacion = SecurityContextHolder.getContext().getAuthentication().getName();

                Usuario usuario = usuarioService.obtenerPorIdentificacion(identificacion);

                return ResponseEntity.ok(
                                registroService.obtenerMisRegistros(usuario));
        }

        // ============================
        // 📤 EXPORTACIÓN DE REGISTROS (USER)
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
         * Exporta los registros del usuario a PDF
         */
        @PostMapping("/exportar/pdf")
        public ResponseEntity<byte[]> exportarPdf(@RequestBody ExportRequest request) {
                try {
                        String identificacion = SecurityContextHolder.getContext()
                                        .getAuthentication().getName();
                        Usuario usuario = usuarioService.obtenerPorIdentificacion(identificacion);

                        LocalDate fechaActual = LocalDate.now(ZONA_COLOMBIA);
                        int mes = request.mes() != null ? request.mes() : fechaActual.getMonthValue();
                        int anio = request.anio() != null ? request.anio() : fechaActual.getYear();

                        byte[] pdfBytes = exportService.exportarPdfUsuario(usuario, mes, anio);

                        String nombreMes = exportService.getNombreMes(mes);
                        String filename = "Registros_" + nombreMes + "_" + anio + ".pdf";

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"" + filename + "\"")
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .body(pdfBytes);
                } catch (Exception e) {
                        throw new RuntimeException("Error al generar PDF: " + e.getMessage());
                }
        }

        /**
         * Exporta los registros del usuario a Excel
         */
        @PostMapping("/exportar/excel")
        public ResponseEntity<byte[]> exportarExcel(@RequestBody ExportRequest request) {
                try {
                        String identificacion = SecurityContextHolder.getContext()
                                        .getAuthentication().getName();
                        Usuario usuario = usuarioService.obtenerPorIdentificacion(identificacion);

                        LocalDate fechaActual = LocalDate.now(ZONA_COLOMBIA);
                        int mes = request.mes() != null ? request.mes() : fechaActual.getMonthValue();
                        int anio = request.anio() != null ? request.anio() : fechaActual.getYear();

                        byte[] excelBytes = exportService.exportarExcelUsuario(usuario, mes, anio);

                        String nombreMes = exportService.getNombreMes(mes);
                        String filename = "Registros_" + nombreMes + "_" + anio + ".xlsx";

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"" + filename + "\"")
                                        .contentType(MediaType.parseMediaType(
                                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                                        .body(excelBytes);
                } catch (Exception e) {
                        throw new RuntimeException("Error al generar Excel: " + e.getMessage());
                }
        }
}
