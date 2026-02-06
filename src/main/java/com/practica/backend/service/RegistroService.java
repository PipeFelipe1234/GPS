package com.practica.backend.service;

import com.practica.backend.dto.MarcarEntradaRequest;
import com.practica.backend.dto.MarcarSalidaRequest;
import com.practica.backend.dto.RegistroFilterRequest;
import com.practica.backend.dto.RegistroResponse;
import com.practica.backend.entity.Registro;
import com.practica.backend.entity.Usuario;
import com.practica.backend.repository.RegistroRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RegistroService {

    private final RegistroRepository registroRepository;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public RegistroService(RegistroRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    /**
     * Parsea una fecha ISO 8601 y retorna LocalDate y LocalTime
     */
    private LocalDateTime parseISODateTime(String iso8601) {
        if (iso8601 == null || iso8601.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(iso8601, ISO_FORMATTER);
        } catch (Exception e) {
            // Si falla el parsing, retorna null
            return null;
        }
    }

    public RegistroResponse marcarEntrada(Usuario usuario, MarcarEntradaRequest request) {

        LocalDate hoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        // Validar precisión GPS
        if (request.precisionMetrosCheckin() != null && request.precisionMetrosCheckin() > 50) {
            throw new RuntimeException("Precisión GPS insuficiente en entrada");
        }

        // Si viene fechaCreacion, usarla; sino, usar la hora actual
        LocalDateTime fechaHoraRegistro = parseISODateTime(request.fechaCreacion());

        if (fechaHoraRegistro != null) {
            hoy = fechaHoraRegistro.toLocalDate();
            horaActual = fechaHoraRegistro.toLocalTime();
        }

        Registro registro = new Registro();
        registro.setUsuario(usuario);
        registro.setFecha(hoy);
        registro.setHoraEntrada(horaActual);
        registro.setLatitudCheckin(request.latitudCheckin());
        registro.setLongitudCheckin(request.longitudCheckin());
        registro.setPrecisionMetrosCheckin(request.precisionMetrosCheckin());

        Registro guardado = registroRepository.save(registro);

        return mapToResponse(guardado);
    }

    public RegistroResponse marcarSalida(
            Usuario usuario,
            MarcarSalidaRequest request) {

        LocalDate hoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        // Si viene fechaCreacion, usarla para obtener la fecha correcta
        LocalDateTime fechaHoraRegistro = parseISODateTime(request.fechaCreacion());

        if (fechaHoraRegistro != null) {
            hoy = fechaHoraRegistro.toLocalDate();
            horaActual = fechaHoraRegistro.toLocalTime();
        }

        Registro registro = registroRepository
                .findByUsuarioAndFechaAndHoraSalidaIsNull(usuario, hoy)
                .orElseThrow(() -> new RuntimeException("No hay entrada sin salida registrada para hoy"));

        // Validar precisión GPS
        if (request.precisionMetros() != null && request.precisionMetros() > 50) {
            throw new RuntimeException("Precisión GPS insuficiente en salida");
        }

        registro.setHoraSalida(horaActual);
        registro.setLatitud(request.latitud());
        registro.setLongitud(request.longitud());
        registro.setPrecisionMetros(request.precisionMetros());
        registro.setReporte(request.reporte());
        registro.setPicture(request.picture());

        Registro guardado = registroRepository.save(registro);

        return mapToResponse(guardado);
    }

    public List<RegistroResponse> obtenerMisRegistros(Usuario usuario) {
        return registroRepository.findAllByUsuario(usuario)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RegistroResponse> obtenerTodos() {
        return registroRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // � FILTRAR REGISTROS CON CRITERIOS PERSONALIZADOS
    public List<RegistroResponse> filtrarRegistros(RegistroFilterRequest filtro) {
        return registroRepository.findByFiltros(
                filtro.getFecha(),
                filtro.getIdentificacion(),
                filtro.getNombres())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // �🔁 Mapper centralizado
    private RegistroResponse mapToResponse(Registro r) {
        return new RegistroResponse(
                r.getId(),
                r.getFecha(),
                r.getHoraEntrada(),
                r.getHoraSalida(),
                r.getLatitud(),
                r.getLongitud(),
                r.getPrecisionMetros(),
                r.getLatitudCheckin(),
                r.getLongitudCheckin(),
                r.getPrecisionMetrosCheckin(),
                r.getReporte(),
                r.getPicture(),
                r.getUsuario().getIdentificacion(),
                r.getUsuario().getNombre(),
                r.getUsuario().getFoto(),
                r.getUsuario().getTelefono());
    }
}
