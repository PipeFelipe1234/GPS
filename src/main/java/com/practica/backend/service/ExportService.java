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
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class ExportService {

    private final RegistroRepository registroRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale LOCALE_ES = new Locale("es", "ES");

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

    // ============================
    // 📊 EXPORTAR A EXCEL
    // ============================

    /**
     * Exporta registros de un mes a Excel (para ADMIN - todos los registros)
     */
    public byte[] exportarExcelAdmin(int mes, int anio) throws Exception {
        List<Registro> registros = registroRepository.findByMesYAnio(mes, anio);
        return generarExcel(registros, mes, anio, true);
    }

    /**
     * Exporta registros de un mes a Excel (para USER - solo sus registros)
     */
    public byte[] exportarExcelUsuario(Usuario usuario, int mes, int anio) throws Exception {
        List<Registro> registros = registroRepository.findByUsuarioAndMesYAnio(usuario, mes, anio);
        return generarExcel(registros, mes, anio, false);
    }

    private byte[] generarExcel(List<Registro> registros, int mes, int anio, boolean esAdmin) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Registros " + getNombreMes(mes) + " " + anio);

            // Estilos
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

            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

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
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, esAdmin ? 11 : 9));

            // Subtítulo con fecha de generación
            org.apache.poi.ss.usermodel.Row subtitleRow = sheet.createRow(1);
            org.apache.poi.ss.usermodel.Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Generado el: " + LocalDate.now().format(DATE_FORMATTER));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, esAdmin ? 11 : 9));

            // Encabezados
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(3);
            String[] headers;
            if (esAdmin) {
                headers = new String[] {
                        "Fecha", "Empleado", "Identificación", "Cargo", "Teléfono",
                        "Hora Entrada", "Hora Salida", "Horas Trabajadas",
                        "Ubicación Entrada", "Ubicación Salida", "Reporte", "Imagen"
                };
            } else {
                headers = new String[] {
                        "Fecha", "Hora Entrada", "Hora Salida", "Horas Trabajadas",
                        "Min. Trabajados", "Ubicación Entrada", "Ubicación Salida",
                        "Reporte", "Estado", "Imagen"
                };
            }

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4500);
            }

            // Datos
            int rowNum = 4;
            for (Registro registro : registros) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                if (esAdmin) {
                    // Datos para ADMIN
                    createExcelCell(row, colNum++, registro.getFecha().format(DATE_FORMATTER), dataStyle);
                    createExcelCell(row, colNum++, registro.getUsuario().getNombre(), dataStyle);
                    createExcelCell(row, colNum++, registro.getUsuario().getIdentificacion(), dataStyle);
                    createExcelCell(row, colNum++, registro.getUsuario().getCargo(), dataStyle);
                    createExcelCell(row, colNum++, registro.getUsuario().getTelefono(), dataStyle);
                    createExcelCell(row, colNum++, formatTime(registro.getHoraEntrada()), dataStyle);
                    createExcelCell(row, colNum++, formatTime(registro.getHoraSalida()), dataStyle);
                    createExcelCell(row, colNum++, formatHorasTrabajadas(registro), dataStyle);
                    createExcelCell(row, colNum++,
                            formatUbicacion(registro.getLatitudCheckin(), registro.getLongitudCheckin()), dataStyle);
                    createExcelCell(row, colNum++, formatUbicacion(registro.getLatitud(), registro.getLongitud()),
                            dataStyle);
                    createExcelCell(row, colNum++, registro.getReporte() != null ? registro.getReporte() : "",
                            dataStyle);
                    createExcelCell(row, colNum++, registro.getPicture() != null ? registro.getPicture() : "Sin imagen",
                            dataStyle);
                } else {
                    // Datos para USER
                    createExcelCell(row, colNum++, registro.getFecha().format(DATE_FORMATTER), dataStyle);
                    createExcelCell(row, colNum++, formatTime(registro.getHoraEntrada()), dataStyle);
                    createExcelCell(row, colNum++, formatTime(registro.getHoraSalida()), dataStyle);
                    createExcelCell(row, colNum++, formatHoras(registro.getHorasTrabajadas()), dataStyle);
                    createExcelCell(row, colNum++, formatMinutos(registro.getMinutosTrabajados()), dataStyle);
                    createExcelCell(row, colNum++,
                            formatUbicacion(registro.getLatitudCheckin(), registro.getLongitudCheckin()), dataStyle);
                    createExcelCell(row, colNum++, formatUbicacion(registro.getLatitud(), registro.getLongitud()),
                            dataStyle);
                    createExcelCell(row, colNum++, registro.getReporte() != null ? registro.getReporte() : "",
                            dataStyle);
                    createExcelCell(row, colNum++, registro.getHoraSalida() == null ? "En curso" : "Finalizado",
                            dataStyle);
                    createExcelCell(row, colNum++, registro.getPicture() != null ? registro.getPicture() : "Sin imagen",
                            dataStyle);
                }
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
     */
    public byte[] exportarPdfAdmin(int mes, int anio) throws Exception {
        List<Registro> registros = registroRepository.findByMesYAnio(mes, anio);
        return generarPdf(registros, mes, anio, true);
    }

    /**
     * Exporta registros de un mes a PDF (para USER)
     */
    public byte[] exportarPdfUsuario(Usuario usuario, int mes, int anio) throws Exception {
        List<Registro> registros = registroRepository.findByUsuarioAndMesYAnio(usuario, mes, anio);
        return generarPdf(registros, mes, anio, false);
    }

    private byte[] generarPdf(List<Registro> registros, int mes, int anio, boolean esAdmin) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        com.lowagie.text.Document document = new com.lowagie.text.Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);
        document.open();

        // Fuentes
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.DARK_GRAY);
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.GRAY);
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font dataFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);

        // Título
        Paragraph title = new Paragraph("REGISTROS DE ASISTENCIA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph(getNombreMes(mes) + " " + anio, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        Paragraph fecha = new Paragraph("Generado el: " + LocalDate.now().format(DATE_FORMATTER), subtitleFont);
        fecha.setAlignment(Element.ALIGN_CENTER);
        fecha.setSpacingAfter(20);
        document.add(fecha);

        // Tabla
        int numColumnas = esAdmin ? 10 : 8;
        PdfPTable table = new PdfPTable(numColumnas);
        table.setWidthPercentage(100);

        // Encabezados
        String[] headers;
        if (esAdmin) {
            headers = new String[] {
                    "Fecha", "Empleado", "Identificación", "Cargo",
                    "H. Entrada", "H. Salida", "Horas Trab.",
                    "Ubic. Entrada", "Ubic. Salida", "Reporte"
            };
        } else {
            headers = new String[] {
                    "Fecha", "H. Entrada", "H. Salida", "Horas Trab.",
                    "Min. Trab.", "Ubic. Entrada", "Ubic. Salida", "Estado"
            };
        }

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(0, 51, 102));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Datos
        for (Registro registro : registros) {
            if (esAdmin) {
                addPdfCell(table, registro.getFecha().format(DATE_FORMATTER), dataFont);
                addPdfCell(table, registro.getUsuario().getNombre(), dataFont);
                addPdfCell(table, registro.getUsuario().getIdentificacion(), dataFont);
                addPdfCell(table, registro.getUsuario().getCargo(), dataFont);
                addPdfCell(table, formatTime(registro.getHoraEntrada()), dataFont);
                addPdfCell(table, formatTime(registro.getHoraSalida()), dataFont);
                addPdfCell(table, formatHorasTrabajadas(registro), dataFont);
                addPdfCell(table, formatUbicacionCorta(registro.getLatitudCheckin(), registro.getLongitudCheckin()),
                        dataFont);
                addPdfCell(table, formatUbicacionCorta(registro.getLatitud(), registro.getLongitud()), dataFont);
                addPdfCell(table, truncate(registro.getReporte(), 30), dataFont);
            } else {
                addPdfCell(table, registro.getFecha().format(DATE_FORMATTER), dataFont);
                addPdfCell(table, formatTime(registro.getHoraEntrada()), dataFont);
                addPdfCell(table, formatTime(registro.getHoraSalida()), dataFont);
                addPdfCell(table, formatHoras(registro.getHorasTrabajadas()), dataFont);
                addPdfCell(table, formatMinutos(registro.getMinutosTrabajados()), dataFont);
                addPdfCell(table, formatUbicacionCorta(registro.getLatitudCheckin(), registro.getLongitudCheckin()),
                        dataFont);
                addPdfCell(table, formatUbicacionCorta(registro.getLatitud(), registro.getLongitud()), dataFont);
                addPdfCell(table, registro.getHoraSalida() == null ? "En curso" : "Finalizado", dataFont);
            }
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
        cell.setPadding(4);
        table.addCell(cell);
    }

    // ============================
    // 📝 EXPORTAR A WORD
    // ============================

    /**
     * Exporta registros de un mes a Word (para ADMIN)
     */
    public byte[] exportarWordAdmin(int mes, int anio) throws Exception {
        List<Registro> registros = registroRepository.findByMesYAnio(mes, anio);
        return generarWord(registros, mes, anio, true);
    }

    /**
     * Exporta registros de un mes a Word (para USER)
     */
    public byte[] exportarWordUsuario(Usuario usuario, int mes, int anio) throws Exception {
        List<Registro> registros = registroRepository.findByUsuarioAndMesYAnio(usuario, mes, anio);
        return generarWord(registros, mes, anio, false);
    }

    private byte[] generarWord(List<Registro> registros, int mes, int anio, boolean esAdmin) throws Exception {
        try (XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Título
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("REGISTROS DE ASISTENCIA");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.addBreak();

            XWPFRun subtitleRun = titleParagraph.createRun();
            subtitleRun.setText(getNombreMes(mes) + " " + anio);
            subtitleRun.setFontSize(14);
            subtitleRun.addBreak();

            XWPFRun dateRun = titleParagraph.createRun();
            dateRun.setText("Generado el: " + LocalDate.now().format(DATE_FORMATTER));
            dateRun.setFontSize(10);
            dateRun.setItalic(true);
            dateRun.addBreak();
            dateRun.addBreak();

            // Tabla
            String[] headers;
            if (esAdmin) {
                headers = new String[] {
                        "Fecha", "Empleado", "Identificación", "Cargo",
                        "H. Entrada", "H. Salida", "Horas Trab.", "Reporte"
                };
            } else {
                headers = new String[] {
                        "Fecha", "H. Entrada", "H. Salida", "Horas",
                        "Minutos", "Reporte", "Estado"
                };
            }

            int numCols = headers.length;
            XWPFTable table = document.createTable(registros.size() + 1, numCols);
            table.setWidth("100%");

            // Encabezados
            XWPFTableRow headerRow = table.getRow(0);
            for (int i = 0; i < headers.length; i++) {
                XWPFTableCell cell = headerRow.getCell(i);
                cell.setColor("003366");
                XWPFParagraph p = cell.getParagraphs().get(0);
                p.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun run = p.createRun();
                run.setText(headers[i]);
                run.setBold(true);
                run.setColor("FFFFFF");
                run.setFontSize(9);
            }

            // Datos
            for (int i = 0; i < registros.size(); i++) {
                Registro registro = registros.get(i);
                XWPFTableRow row = table.getRow(i + 1);

                if (esAdmin) {
                    setWordCell(row, 0, registro.getFecha().format(DATE_FORMATTER));
                    setWordCell(row, 1, registro.getUsuario().getNombre());
                    setWordCell(row, 2, registro.getUsuario().getIdentificacion());
                    setWordCell(row, 3, registro.getUsuario().getCargo());
                    setWordCell(row, 4, formatTime(registro.getHoraEntrada()));
                    setWordCell(row, 5, formatTime(registro.getHoraSalida()));
                    setWordCell(row, 6, formatHorasTrabajadas(registro));
                    setWordCell(row, 7, truncate(registro.getReporte(), 50));
                } else {
                    setWordCell(row, 0, registro.getFecha().format(DATE_FORMATTER));
                    setWordCell(row, 1, formatTime(registro.getHoraEntrada()));
                    setWordCell(row, 2, formatTime(registro.getHoraSalida()));
                    setWordCell(row, 3, formatHoras(registro.getHorasTrabajadas()));
                    setWordCell(row, 4, formatMinutos(registro.getMinutosTrabajados()));
                    setWordCell(row, 5, truncate(registro.getReporte(), 50));
                    setWordCell(row, 6, registro.getHoraSalida() == null ? "En curso" : "Finalizado");
                }
            }

            // Resumen
            XWPFParagraph resumenParagraph = document.createParagraph();
            resumenParagraph.setSpacingBefore(400);
            XWPFRun resumenRun = resumenParagraph.createRun();
            resumenRun.addBreak();
            resumenRun.setText("Total de registros: " + registros.size());
            resumenRun.setBold(true);

            document.write(out);
            return out.toByteArray();
        }
    }

    private void setWordCell(XWPFTableRow row, int col, String value) {
        XWPFTableCell cell = row.getCell(col);
        XWPFParagraph p = cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setText(value != null ? value : "");
        run.setFontSize(8);
    }

    // ============================
    // 🔧 UTILIDADES
    // ============================

    private String formatTime(java.time.LocalTime time) {
        if (time == null)
            return "-";
        return time.format(TIME_FORMATTER);
    }

    private String formatHorasTrabajadas(Registro registro) {
        if (registro.getHorasTrabajadas() == null && registro.getMinutosTrabajados() == null) {
            if (registro.getHoraSalida() == null)
                return "En curso";
            return "-";
        }
        int horas = registro.getHorasTrabajadas() != null ? registro.getHorasTrabajadas() : 0;
        int minutos = registro.getMinutosTrabajados() != null ? registro.getMinutosTrabajados() % 60 : 0;
        return horas + "h " + minutos + "m";
    }

    private String formatHoras(Integer horas) {
        if (horas == null)
            return "-";
        return horas + "h";
    }

    private String formatMinutos(Integer minutos) {
        if (minutos == null)
            return "-";
        return minutos + " min";
    }

    private String formatUbicacion(Double lat, Double lng) {
        if (lat == null || lng == null)
            return "Sin ubicación";
        return String.format("%.6f, %.6f", lat, lng);
    }

    private String formatUbicacionCorta(Double lat, Double lng) {
        if (lat == null || lng == null)
            return "-";
        return String.format("%.4f, %.4f", lat, lng);
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "";
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
