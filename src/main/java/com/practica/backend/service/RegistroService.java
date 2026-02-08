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
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistroService {

    private final RegistroRepository registroRepository;
    private final NotificacionService notificacionService;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public RegistroService(RegistroRepository registroRepository, NotificacionService notificacionService) {
        this.registroRepository = registroRepository;
        this.notificacionService = notificacionService;
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

        // 📲 ENVIAR NOTIFICACIÓN A LOS ADMINS
        enviarNotificacionEntrada(guardado);

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

        // ⏱️ Calcular y guardar horas trabajadas
        Duration duracion = Duration.between(registro.getHoraEntrada(), horaActual);
        registro.setHorasTrabajadas((int) duracion.toHours());

        Registro guardado = registroRepository.save(registro);

        // 📲 ENVIAR NOTIFICACIÓN A LOS ADMINS
        enviarNotificacionSalida(guardado);

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
        // ⏱️ Calcular horas trabajadas
        String horasTrabajadas;
        Long minutosTrabajados;
        Boolean enCurso = (r.getHoraSalida() == null);

        if (enCurso) {
            // 🟢 Turno en curso - calcular en tiempo real
            Duration duracion = Duration.between(r.getHoraEntrada(), LocalTime.now());
            minutosTrabajados = duracion.toMinutes();
        } else {
            // 🔴 Turno finalizado - usar valor guardado o calcular
            if (r.getHorasTrabajadas() != null) {
                minutosTrabajados = r.getHorasTrabajadas();
            } else {
                Duration duracion = Duration.between(r.getHoraEntrada(), r.getHoraSalida());
                minutosTrabajados = duracion.toMinutes();
            }
        }

        // Formatear a "HH:mm:ss"
        long horas = minutosTrabajados / 60;
        long minutos = Math.abs(minutosTrabajados % 60);
        horasTrabajadas = String.format("%02d:%02d:00", horas, minutos);

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
                r.getUsuario().getTelefono(),
                horasTrabajadas,
                minutosTrabajados,
                enCurso);
    }

    // 📲 NOTIFICACIÓN DE ENTRADA
    private void enviarNotificacionEntrada(Registro registro) {
        try {
            Map<String, String> datos = new HashMap<>();
            datos.put("tipo", "ENTRADA");
            datos.put("registroId", registro.getId().toString());
            datos.put("usuarioId", registro.getUsuario().getId().toString());
            datos.put("fecha", registro.getFecha().toString());
            datos.put("hora", registro.getHoraEntrada().toString());

            String titulo = "✅ Entrada Registrada";
            String mensaje = registro.getUsuario().getNombre() + " marcó entrada a las " +
                    registro.getHoraEntrada().toString();

            notificacionService.enviarNotificacionAAdmins(titulo, mensaje, datos);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar notificación de entrada: " + e.getMessage());
        }
    }

    // 📲 NOTIFICACIÓN DE SALIDA
    private void enviarNotificacionSalida(Registro registro) {
        try {
            Map<String, String> datos = new HashMap<>();
            datos.put("tipo", "SALIDA");
            datos.put("registroId", registro.getId().toString());
            datos.put("usuarioId", registro.getUsuario().getId().toString());
            datos.put("fecha", registro.getFecha().toString());
            datos.put("hora", registro.getHoraSalida().toString());

            String titulo = "🚪 Salida Registrada";
            String mensaje = registro.getUsuario().getNombre() + " marcó salida a las " +
                    registro.getHoraSalida().toString();

            notificacionService.enviarNotificacionAAdmins(titulo, mensaje, datos);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar notificación de salida: " + e.getMessage());
        }
    }
}
