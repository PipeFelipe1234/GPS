package com.practica.backend.service;

import com.practica.backend.dto.MarcarEntradaRequest;
import com.practica.backend.dto.MarcarSalidaRequest;
import com.practica.backend.dto.RegistroResponse;
import com.practica.backend.entity.Registro;
import com.practica.backend.entity.Usuario;
import com.practica.backend.repository.RegistroRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class RegistroService {

    private final RegistroRepository registroRepository;

    public RegistroService(RegistroRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    public RegistroResponse marcarEntrada(Usuario usuario, MarcarEntradaRequest request) {

        LocalDate hoy = LocalDate.now();

        // Validar precisión GPS
        if (request.precisionMetrosCheckin() != null && request.precisionMetrosCheckin() > 50) {
            throw new RuntimeException("Precisión GPS insuficiente en entrada");
        }

        Registro registro = new Registro();
        registro.setUsuario(usuario);
        registro.setFecha(hoy);
        registro.setHoraEntrada(LocalTime.now());
        registro.setLatitudCheckin(request.latitudCheckin());
        registro.setLongitudCheckin(request.longitudCheckin());
        registro.setPrecisionMetrosCheckin(request.precisionMetrosCheckin());

        Registro guardado = registroRepository.save(registro);

        return mapToResponse(guardado);
    }

    public RegistroResponse marcarSalida(
            Usuario usuario,
            MarcarSalidaRequest request) {

        Registro registro = registroRepository
                .findByUsuarioAndFecha(usuario, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No hay entrada registrada"));

        // Validar que tenga entrada sin salida
        if (registro.getHoraSalida() != null) {
            throw new RuntimeException("Ya marcó salida para este registro");
        }

        // Validar precisión GPS
        if (request.precisionMetrosCheckout() != null && request.precisionMetrosCheckout() > 50) {
            throw new RuntimeException("Precisión GPS insuficiente en salida");
        }

        registro.setHoraSalida(LocalTime.now());
        registro.setLatitudCheckout(request.latitudCheckout());
        registro.setLongitudCheckout(request.longitudCheckout());
        registro.setPrecisionMetrosCheckout(request.precisionMetrosCheckout());
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

    // 🔁 Mapper centralizado
    private RegistroResponse mapToResponse(Registro r) {
        return new RegistroResponse(
                r.getId(),
                r.getFecha(),
                r.getHoraEntrada(),
                r.getHoraSalida(),
                r.getLatitudCheckin(),
                r.getLongitudCheckin(),
                r.getPrecisionMetrosCheckin(),
                r.getLatitudCheckout(),
                r.getLongitudCheckout(),
                r.getPrecisionMetrosCheckout(),
                r.getReporte(),
                r.getPicture(),
                r.getUsuario().getIdentificacion());
    }
}
