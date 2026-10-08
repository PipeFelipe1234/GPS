package com.practica.backend.service;

import com.practica.backend.dto.MarcarEntradaRequest;
import com.practica.backend.dto.MarcarSalidaRequest;
import com.practica.backend.dto.RegistroFilterRequest;
import com.practica.backend.dto.RegistroResponse;
import com.practica.backend.entity.Registro;
import com.practica.backend.entity.Usuario;
import com.practica.backend.exception.RegistroException;
import com.practica.backend.repository.RegistroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");
    private static final LocalTime HORA_CIERRE_AUTOMATICO = LocalTime.of(23, 55);

    public RegistroService(RegistroRepository registroRepository,
            NotificacionService notificacionService) {
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

    @Transactional
    public RegistroResponse marcarEntrada(Usuario usuario, MarcarEntradaRequest request) {

        LocalDate hoy = LocalDate.now(ZONA_COLOMBIA);
        LocalTime horaActual = LocalTime.now(ZONA_COLOMBIA);

        // Validar precisión GPS
        if (request.precisionMetrosCheckin() != null && request.precisionMetrosCheckin() > 50) {
            throw new RegistroException(HttpStatus.UNPROCESSABLE_ENTITY, "Precisión GPS insuficiente en entrada");
        }

        // Si viene fechaCreacion, usarla; sino, usar la hora actual
        LocalDateTime fechaHoraRegistro = parseISODateTime(request.fechaCreacion());

        if (fechaHoraRegistro != null) {
            hoy = fechaHoraRegistro.toLocalDate();
            horaActual = fechaHoraRegistro.toLocalTime();
        }

        Registro abierto = registroRepository.findUltimoRegistroSinSalida(usuario).orElse(null);

        if (abierto != null) {
            if (!abierto.getFecha().isBefore(hoy)) {
                throw new RegistroException(HttpStatus.CONFLICT,
                        "Ya tienes un turno abierto. Marca la salida antes de registrar una nueva entrada");
            }
            // Turno olvidado de un día anterior: se cierra para permitir la nueva entrada
            cerrarAutomaticamente(abierto);
        }

        Registro registro = new Registro();
        registro.setUsuario(usuario);
        registro.setFecha(hoy);
        registro.setHoraEntrada(horaActual);
        registro.setLatitudCheckin(request.latitudCheckin());
        registro.setLongitudCheckin(request.longitudCheckin());
        registro.setPrecisionMetrosCheckin(request.precisionMetrosCheckin());
        registro.setUbicacionEntrada(request.ubicacionEntrada());

        Registro guardado = registroRepository.save(registro);

        // 📲 ENVIAR NOTIFICACIÓN A LOS ADMINS
        enviarNotificacionEntrada(guardado);

        return mapToResponse(guardado);
    }

    @Transactional
    public RegistroResponse marcarSalida(
            Usuario usuario,
            MarcarSalidaRequest request) {

        LocalDate fechaSalida = LocalDate.now(ZONA_COLOMBIA);
        LocalTime horaSalida = LocalTime.now(ZONA_COLOMBIA);

        // Si viene fechaCreacion, usarla para obtener la fecha correcta
        LocalDateTime fechaHoraRegistro = parseISODateTime(request.fechaCreacion());

        if (fechaHoraRegistro != null) {
            fechaSalida = fechaHoraRegistro.toLocalDate();
            horaSalida = fechaHoraRegistro.toLocalTime();
        }

        // 🔄 Buscar cualquier registro sin salida (sin importar la fecha de entrada)
        Registro registro = registroRepository
                .findUltimoRegistroSinSalida(usuario)
                .orElseThrow(() -> new RegistroException(HttpStatus.CONFLICT,
                        "No tienes un turno abierto para marcar salida"));

        // Validar precisión GPS
        if (request.precisionMetros() != null && request.precisionMetros() > 50) {
            throw new RegistroException(HttpStatus.UNPROCESSABLE_ENTITY, "Precisión GPS insuficiente en salida");
        }

        LocalDateTime entradaRegistro = LocalDateTime.of(registro.getFecha(), registro.getHoraEntrada());
        if (LocalDateTime.of(fechaSalida, horaSalida).isBefore(entradaRegistro)) {
            throw new RegistroException(HttpStatus.CONFLICT, "La salida no puede ser anterior a la entrada");
        }

        registro.setHoraSalida(horaSalida);
        registro.setLatitud(request.latitud());
        registro.setLongitud(request.longitud());
        registro.setPrecisionMetros(request.precisionMetros());
        registro.setReporte(request.reporte());
        registro.setPicture(request.picture());
        registro.setUbicacionSalida(request.ubicacionSalida());

        // ⏱️ Calcular horas trabajadas considerando que pueden ser días diferentes
        LocalDateTime fechaHoraEntrada = LocalDateTime.of(registro.getFecha(), registro.getHoraEntrada());
        LocalDateTime fechaHoraSalidaFinal = LocalDateTime.of(fechaSalida, horaSalida);
        Duration duracion = Duration.between(fechaHoraEntrada, fechaHoraSalidaFinal);

        registro.setHorasTrabajadas((int) duracion.toHours());
        registro.setMinutosTrabajados((int) duracion.toMinutes());

        Registro guardado = registroRepository.save(registro);

        // 📲 ENVIAR NOTIFICACIÓN A LOS ADMINS
        enviarNotificacionSalida(guardado);

        return mapToResponse(guardado);
    }

    public List<RegistroResponse> obtenerMisRegistros(Usuario usuario) {
        return registroRepository.findAllByUsuarioOrderByFechaAscHoraEntradaAsc(usuario)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Cierra los turnos abiertos con fecha hasta {@code hasta} (inclusive) marcando
     * salida a las 23:55 de su día. Retorna la cantidad de turnos cerrados.
     */
    @Transactional
    public int cerrarTurnosOlvidados(LocalDate hasta) {
        List<Registro> abiertos = registroRepository.findAllByHoraSalidaIsNullAndFechaLessThanEqual(hasta);
        abiertos.forEach(this::cerrarAutomaticamente);
        return abiertos.size();
    }

    private void cerrarAutomaticamente(Registro registro) {
        // Si la entrada fue después de las 23:55 la salida no puede quedar antes de ella
        LocalTime salida = registro.getHoraEntrada().isAfter(HORA_CIERRE_AUTOMATICO)
                ? registro.getHoraEntrada()
                : HORA_CIERRE_AUTOMATICO;
        Duration duracion = Duration.between(registro.getHoraEntrada(), salida);

        registro.setHoraSalida(salida);
        registro.setHorasTrabajadas((int) duracion.toHours());
        registro.setMinutosTrabajados((int) duracion.toMinutes());
        registroRepository.save(registro);
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

    // 🔁 Mapper centralizado
    private RegistroResponse mapToResponse(Registro r) {
        Boolean enCurso = (r.getHoraSalida() == null);
        Integer horasTrabajadas;
        Integer minutosTrabajados;

        if (enCurso) {
            // 🟢 Turno en curso - calcular horas y minutos en tiempo real
            Duration duracion = Duration.between(
                    LocalDateTime.of(r.getFecha(), r.getHoraEntrada()),
                    LocalDateTime.now(ZONA_COLOMBIA));
            horasTrabajadas = (int) duracion.toHours();
            minutosTrabajados = (int) duracion.toMinutes();
        } else {
            // 🔴 Turno finalizado - usar valor guardado o calcular
            if (r.getHorasTrabajadas() != null) {
                horasTrabajadas = r.getHorasTrabajadas();
            } else {
                Duration duracion = Duration.between(r.getHoraEntrada(), r.getHoraSalida());
                horasTrabajadas = (int) duracion.toHours();
            }
            if (r.getMinutosTrabajados() != null) {
                minutosTrabajados = r.getMinutosTrabajados();
            } else {
                Duration duracion = Duration.between(r.getHoraEntrada(), r.getHoraSalida());
                minutosTrabajados = (int) duracion.toMinutes();
            }
        }

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
                r.getUbicacionEntrada(),
                r.getUbicacionSalida(),
                r.getUsuario().getIdentificacion(),
                r.getUsuario().getNombre(),
                r.getUsuario().getFoto(),
                r.getUsuario().getTelefono(),
                r.getUsuario().getCargo(),
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
            String mensaje = registro.getUsuario().getNombre() + " marcó Entrada";

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
            String mensaje = registro.getUsuario().getNombre() + " marcó Salida";

            notificacionService.enviarNotificacionAAdmins(titulo, mensaje, datos);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar notificación de salida: " + e.getMessage());
        }
    }
}
