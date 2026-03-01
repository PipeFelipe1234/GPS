package com.practica.backend.service;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.practica.backend.entity.Registro;
import com.practica.backend.entity.Usuario;
import com.practica.backend.repository.RegistroRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Servicio para exportar registros a PDF y Excel.
 * 
 * ENCABEZADOS (10 columnas):
 * Fecha | Identificación | Empleado | Hora Entrada | Ubicación Entrada |
 * Hora Salida | Ubicación Salida | Reporte | Foto | Horas Trabajadas
 */
@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    private final RegistroRepository registroRepository;
    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale LOCALE_ES = new Locale("es", "ES");

    // Encabezados de las columnas (10 columnas - sin latitud/longitud)
    private static final String[] HEADERS = {
            "Fecha", "Identificación", "Empleado", "Hora Entrada", "Ubicación Entrada",
            "Hora Salida", "Ubicación Salida", "Reporte", "Foto", "Horas Trabajadas"
    };

    public ExportService(RegistroRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    /**
     * Obtiene el nombre del mes en español
     */
    public String getNombreMes(int mes) {
        return LocalDate.of(2024, mes, 1)
                .getMonth()
                .getDisplayName(TextStyle.FULL, LOCALE_ES)
                .toUpperCase();
    }

    /**
     * Calcula el primer día del mes
     */
    private LocalDate getPrimerDiaMes(int mes, int anio) {
        return LocalDate.of(anio, mes, 1);
    }

    /**
     * Calcula el último día del mes
     */
    private LocalDate getUltimoDiaMes(int mes, int anio) {
        return LocalDate.of(anio, mes, 1).withDayOfMonth(
                LocalDate.of(anio, mes, 1).lengthOfMonth());
    }

    // ============================
    // 📊 EXPORTAR A EXCEL
    // ============================

    /**
     * Exporta registros de un mes a Excel (para ADMIN - todos los registros)
     * Usa findAll() + filtrado en Java para máxima compatibilidad
     */
    public byte[] exportarExcelAdmin(int mes, int anio) throws Exception {
        LocalDate fechaInicio = getPrimerDiaMes(mes, anio);
        LocalDate fechaFin = getUltimoDiaMes(mes, anio);

        logger.info("Exportando Excel - Mes: {}, Año: {}, Rango: {} a {}", mes, anio, fechaInicio, fechaFin);

        // Obtener TODOS los registros y filtrar en Java (igual que
        // /api/admin/registros)
        List<Registro> todosRegistros = registroRepository.findAll();
        logger.info("Total registros en BD: {}", todosRegistros.size());

        List<Registro> registros = todosRegistros.stream()
                .filter(r -> r.getFecha() != null)
                .filter(r -> !r.getFecha().isBefore(fechaInicio) && !r.getFecha().isAfter(fechaFin))
                .sorted((r1, r2) -> {
                    int fechaCompare = r1.getFecha().compareTo(r2.getFecha());
                    if (fechaCompare != 0)
                        return fechaCompare;
                    if (r1.getHoraEntrada() != null && r2.getHoraEntrada() != null) {
                        return r1.getHoraEntrada().compareTo(r2.getHoraEntrada());
                    }
                    return 0;
                })
                .toList();

        logger.info("Registros filtrados para mes {}/{}: {}", mes, anio, registros.size());

        return generarExcel(registros, mes, anio);
    }

    /**
     * Exporta registros de un mes a Excel (para USER - solo sus registros)
     */
    public byte[] exportarExcelUsuario(Usuario usuario, int mes, int anio) throws Exception {
        LocalDate fechaInicio = getPrimerDiaMes(mes, anio);
        LocalDate fechaFin = getUltimoDiaMes(mes, anio);

        // Obtener registros del usuario y filtrar por fecha
        List<Registro> todosRegistros = registroRepository.findAllByUsuario(usuario);
        List<Registro> registros = todosRegistros.stream()
                .filter(r -> r.getFecha() != null)
                .filter(r -> !r.getFecha().isBefore(fechaInicio) && !r.getFecha().isAfter(fechaFin))
                .sorted((r1, r2) -> {
                    int fechaCompare = r1.getFecha().compareTo(r2.getFecha());
                    if (fechaCompare != 0)
                        return fechaCompare;
                    if (r1.getHoraEntrada() != null && r2.getHoraEntrada() != null) {
                        return r1.getHoraEntrada().compareTo(r2.getHoraEntrada());
                    }
                    return 0;
                })
                .toList();

        return generarExcel(registros, mes, anio);
    }

    private byte[] generarExcel(List<Registro> registros, int mes, int anio) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Registros " + getNombreMes(mes) + " " + anio);

            // Estilos para encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Estilo para título
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo para datos
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setWrapText(true);

            // Título
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REGISTROS DE ASISTENCIA - " + getNombreMes(mes) + " " + anio);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // Subtítulo con fecha de generación
            org.apache.poi.ss.usermodel.Row subtitleRow = sheet.createRow(1);
            org.apache.poi.ss.usermodel.Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Generado el: " + LocalDate.now(ZONA_COLOMBIA).format(DATE_FORMATTER));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, HEADERS.length - 1));

            // Encabezados
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(3);
            for (int i = 0; i < HEADERS.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // Datos
            int rowNum = 4;
            for (Registro registro : registros) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                int col = 0;

                // Fecha
                createExcelCell(row, col++, registro.getFecha().format(DATE_FORMATTER), dataStyle);
                // Identificación
                createExcelCell(row, col++, registro.getUsuario().getIdentificacion(), dataStyle);
                // Empleado
                createExcelCell(row, col++, registro.getUsuario().getNombre(), dataStyle);
                // Hora Entrada (convertida a hora Colombia)
                createExcelCell(row, col++, formatTimeToColombiaZone(registro.getHoraEntrada()), dataStyle);
                // Ubicación Entrada (dirección textual)
                createExcelCell(row, col++, formatUbicacionEntrada(registro), dataStyle);
                // Hora Salida (convertida a hora Colombia)
                createExcelCell(row, col++, formatTimeToColombiaZone(registro.getHoraSalida()), dataStyle);
                // Ubicación Salida (dirección textual)
                createExcelCell(row, col++, formatUbicacionSalida(registro), dataStyle);
                // Reporte
                createExcelCell(row, col++, registro.getReporte() != null ? registro.getReporte() : "", dataStyle);
                // Foto
                createExcelCell(row, col++, registro.getPicture() != null ? registro.getPicture() : "", dataStyle);
                // Horas Trabajadas
                createExcelCell(row, col++, formatHorasTrabajadas(registro), dataStyle);
            }

            // Resumen al final
            rowNum += 2;
            org.apache.poi.ss.usermodel.Row resumenRow = sheet.createRow(rowNum);
            org.apache.poi.ss.usermodel.Cell resumenCell = resumenRow.createCell(0);
            resumenCell.setCellValue("Total de registros: " + registros.size());
            resumenCell.setCellStyle(titleStyle);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createExcelCell(org.apache.poi.ss.usermodel.Row row, int col, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    // ============================
    // 📄 EXPORTAR A PDF
    // ============================

    /**
     * Exporta registros de un mes a PDF (para ADMIN)
     * Usa findAll() + filtrado en Java para máxima compatibilidad
     */
    public byte[] exportarPdfAdmin(int mes, int anio) throws Exception {
        LocalDate fechaInicio = getPrimerDiaMes(mes, anio);
        LocalDate fechaFin = getUltimoDiaMes(mes, anio);

        logger.info("Exportando PDF - Mes: {}, Año: {}, Rango: {} a {}", mes, anio, fechaInicio, fechaFin);

        // Obtener TODOS los registros y filtrar en Java (igual que
        // /api/admin/registros)
        List<Registro> todosRegistros = registroRepository.findAll();
        logger.info("Total registros en BD: {}", todosRegistros.size());

        List<Registro> registros = todosRegistros.stream()
                .filter(r -> r.getFecha() != null)
                .filter(r -> !r.getFecha().isBefore(fechaInicio) && !r.getFecha().isAfter(fechaFin))
                .sorted((r1, r2) -> {
                    int fechaCompare = r1.getFecha().compareTo(r2.getFecha());
                    if (fechaCompare != 0)
                        return fechaCompare;
                    if (r1.getHoraEntrada() != null && r2.getHoraEntrada() != null) {
                        return r1.getHoraEntrada().compareTo(r2.getHoraEntrada());
                    }
                    return 0;
                })
                .toList();

        logger.info("Registros filtrados para mes {}/{}: {}", mes, anio, registros.size());

        return generarPdf(registros, mes, anio);
    }

    /**
     * Exporta registros de un mes a PDF (para USER)
     */
    public byte[] exportarPdfUsuario(Usuario usuario, int mes, int anio) throws Exception {
        LocalDate fechaInicio = getPrimerDiaMes(mes, anio);
        LocalDate fechaFin = getUltimoDiaMes(mes, anio);

        // Obtener registros del usuario y filtrar por fecha
        List<Registro> todosRegistros = registroRepository.findAllByUsuario(usuario);
        List<Registro> registros = todosRegistros.stream()
                .filter(r -> r.getFecha() != null)
                .filter(r -> !r.getFecha().isBefore(fechaInicio) && !r.getFecha().isAfter(fechaFin))
                .sorted((r1, r2) -> {
                    int fechaCompare = r1.getFecha().compareTo(r2.getFecha());
                    if (fechaCompare != 0)
                        return fechaCompare;
                    if (r1.getHoraEntrada() != null && r2.getHoraEntrada() != null) {
                        return r1.getHoraEntrada().compareTo(r2.getHoraEntrada());
                    }
                    return 0;
                })
                .toList();

        return generarPdf(registros, mes, anio);
    }

    private byte[] generarPdf(List<Registro> registros, int mes, int anio) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Usar landscape para que quepan todas las columnas
        com.lowagie.text.Document document = new com.lowagie.text.Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);
        document.open();

        // Fuentes
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.DARK_GRAY);
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.GRAY);
        Font headerFont = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);
        Font dataFont = new Font(Font.HELVETICA, 6, Font.NORMAL, Color.BLACK);

        // Título
        Paragraph title = new Paragraph("REGISTROS DE ASISTENCIA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph(getNombreMes(mes) + " " + anio, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        Paragraph fecha = new Paragraph("Generado el: " + LocalDate.now(ZONA_COLOMBIA).format(DATE_FORMATTER),
                subtitleFont);
        fecha.setAlignment(Element.ALIGN_CENTER);
        fecha.setSpacingAfter(20);
        document.add(fecha);

        // Tabla con 10 columnas
        PdfPTable table = new PdfPTable(HEADERS.length);
        table.setWidthPercentage(100);

        // Anchos relativos de las columnas (10 columnas)
        float[] columnWidths = { 8, 10, 14, 7, 18, 7, 18, 8, 5, 7 };
        table.setWidths(columnWidths);

        // Encabezados
        for (String header : HEADERS) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(0, 51, 102));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4);
            table.addCell(cell);
        }

        // Datos
        for (Registro registro : registros) {
            // Fecha
            addPdfCell(table, registro.getFecha().format(DATE_FORMATTER), dataFont);
            // Identificación
            addPdfCell(table, registro.getUsuario().getIdentificacion(), dataFont);
            // Empleado
            addPdfCell(table, registro.getUsuario().getNombre(), dataFont);
            // Hora Entrada (convertida a hora Colombia)
            addPdfCell(table, formatTimeToColombiaZone(registro.getHoraEntrada()), dataFont);
            // Ubicación Entrada (dirección textual)
            addPdfCell(table, truncate(formatUbicacionEntrada(registro), 35), dataFont);
            // Hora Salida (convertida a hora Colombia)
            addPdfCell(table, formatTimeToColombiaZone(registro.getHoraSalida()), dataFont);
            // Ubicación Salida (dirección textual)
            addPdfCell(table, truncate(formatUbicacionSalida(registro), 35), dataFont);
            // Reporte
            addPdfCell(table, truncate(registro.getReporte(), 20), dataFont);
            // Foto
            addPdfCell(table, registro.getPicture() != null ? "Sí" : "No", dataFont);
            // Horas Trabajadas
            addPdfCell(table, formatHorasTrabajadas(registro), dataFont);
        }

        document.add(table);

        // Resumen
        Paragraph resumen = new Paragraph("\nTotal de registros: " + registros.size(), subtitleFont);
        resumen.setSpacingBefore(20);
        document.add(resumen);

        document.close();
        return out.toByteArray();
    }

    private void addPdfCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3);
        table.addCell(cell);
    }

    // ============================
    // 🔧 UTILIDADES
    // ============================

    /**
     * Convierte hora almacenada (asumida en UTC) a hora Colombia.
     * Los registros antiguos se guardaron en UTC, así que restamos 5 horas.
     */
    private String formatTimeToColombiaZone(java.time.LocalTime time) {
        if (time == null)
            return "-";
        // Convertir de UTC a Colombia (restar 5 horas)
        java.time.LocalTime horaColombina = time.minusHours(5);
        return horaColombina.format(TIME_FORMATTER);
    }

    private String formatTime(java.time.LocalTime time) {
        if (time == null)
            return "-";
        return time.format(TIME_FORMATTER);
    }

    private String formatUbicacionEntrada(Registro registro) {
        // Usar el campo de dirección textual
        if (registro.getUbicacionEntrada() != null && !registro.getUbicacionEntrada().trim().isEmpty()) {
            return registro.getUbicacionEntrada();
        }
        // Fallback: si no hay dirección pero hay coordenadas, mostrar coordenadas
        if (registro.getLatitudCheckin() != null && registro.getLongitudCheckin() != null) {
            return String.format("%.4f, %.4f", registro.getLatitudCheckin(), registro.getLongitudCheckin());
        }
        return "-";
    }

    private String formatUbicacionSalida(Registro registro) {
        // Usar el campo de dirección textual
        if (registro.getUbicacionSalida() != null && !registro.getUbicacionSalida().trim().isEmpty()) {
            return registro.getUbicacionSalida();
        }
        // Fallback: si no hay dirección pero hay coordenadas, mostrar coordenadas
        if (registro.getLatitud() != null && registro.getLongitud() != null) {
            return String.format("%.4f, %.4f", registro.getLatitud(), registro.getLongitud());
        }
        return "-";
    }

    private String formatHorasTrabajadas(Registro registro) {
        if (registro.getMinutosTrabajados() == null) {
            if (registro.getHoraSalida() == null)
                return "En curso";
            return "-";
        }
        int totalMinutos = registro.getMinutosTrabajados();
        int horas = totalMinutos / 60;
        int minutos = totalMinutos % 60;
        return horas + "h " + minutos + "m";
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "";
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
