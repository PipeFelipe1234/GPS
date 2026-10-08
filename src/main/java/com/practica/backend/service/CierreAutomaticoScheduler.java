package com.practica.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class CierreAutomaticoScheduler {

    private static final Logger log = LoggerFactory.getLogger(CierreAutomaticoScheduler.class);
    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    private final RegistroService registroService;

    public CierreAutomaticoScheduler(RegistroService registroService) {
        this.registroService = registroService;
    }

    // Todos los días a las 23:55 (hora Colombia): cierra los turnos que quedaron abiertos
    @Scheduled(cron = "0 55 23 * * *", zone = "America/Bogota")
    public void cerrarTurnosAbiertos() {
        int cerrados = registroService.cerrarTurnosOlvidados(LocalDate.now(ZONA_COLOMBIA));
        log.info("Cierre automático 23:55: {} turno(s) cerrado(s)", cerrados);
    }

    // Recupera turnos de días anteriores si el servidor estuvo apagado a las 23:55
    @EventListener(ApplicationReadyEvent.class)
    public void cerrarTurnosPendientesAlIniciar() {
        int cerrados = registroService.cerrarTurnosOlvidados(LocalDate.now(ZONA_COLOMBIA).minusDays(1));
        log.info("Cierre automático al iniciar: {} turno(s) de días anteriores cerrado(s)", cerrados);
    }
}
