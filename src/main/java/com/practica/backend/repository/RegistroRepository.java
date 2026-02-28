package com.practica.backend.repository;

import com.practica.backend.entity.Registro;
import com.practica.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface RegistroRepository extends JpaRepository<Registro, Long> {

        Optional<Registro> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);

        Optional<Registro> findByUsuarioAndFechaAndHoraSalidaIsNull(Usuario usuario, LocalDate fecha);

        // 🔍 Buscar el último registro sin salida (sin importar la fecha)
        @Query("SELECT r FROM Registro r WHERE r.usuario = :usuario AND r.horaSalida IS NULL ORDER BY r.fecha DESC, r.horaEntrada DESC")
        Optional<Registro> findUltimoRegistroSinSalida(@Param("usuario") Usuario usuario);

        List<Registro> findAllByUsuario(Usuario usuario);

        // 🔍 FILTROS PERSONALIZADOS PARA ADMIN
        @Query("SELECT r FROM Registro r JOIN r.usuario u WHERE " +
                        "(:fecha IS NULL OR r.fecha = :fecha) AND " +
                        "(:identificacion IS NULL OR LOWER(u.identificacion) LIKE LOWER(CONCAT('%', :identificacion, '%'))) AND "
                        +
                        "(:nombres IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombres, '%')))")
        List<Registro> findByFiltros(
                        @Param("fecha") LocalDate fecha,
                        @Param("identificacion") String identificacion,
                        @Param("nombres") String nombres);

        // 📅 OBTENER REGISTROS POR RANGO DE FECHAS
        @Query("SELECT r FROM Registro r WHERE r.fecha >= :fechaInicio AND r.fecha <= :fechaFin ORDER BY r.fecha ASC, r.horaEntrada ASC")
        List<Registro> findByFechaRange(
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

        // 📅 OBTENER REGISTROS DE UN USUARIO POR RANGO DE FECHAS
        @Query("SELECT r FROM Registro r WHERE r.usuario = :usuario AND r.fecha >= :fechaInicio AND r.fecha <= :fechaFin ORDER BY r.fecha ASC, r.horaEntrada ASC")
        List<Registro> findByUsuarioAndFechaRange(
                        @Param("usuario") Usuario usuario,
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

        // 📅 OBTENER REGISTROS POR MES Y AÑO (usando rango de fechas para mayor
        // compatibilidad)
        @Query("SELECT r FROM Registro r WHERE r.fecha >= :fechaInicio AND r.fecha <= :fechaFin ORDER BY r.fecha ASC, r.horaEntrada ASC")
        List<Registro> findByMesYAnioRange(
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

        // 📅 OBTENER REGISTROS DE UN USUARIO POR MES Y AÑO (usando rango de fechas para
        // mayor compatibilidad)
        @Query("SELECT r FROM Registro r WHERE r.usuario = :usuario AND r.fecha >= :fechaInicio AND r.fecha <= :fechaFin ORDER BY r.fecha ASC, r.horaEntrada ASC")
        List<Registro> findByUsuarioAndMesYAnioRange(
                        @Param("usuario") Usuario usuario,
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

        // 🗑️ ELIMINAR REGISTROS ANTERIORES A UNA FECHA
        @Modifying
        @Transactional
        @Query("DELETE FROM Registro r WHERE r.fecha < :fechaLimite")
        int deleteByFechaAnteriorA(@Param("fechaLimite") LocalDate fechaLimite);

        // 🗑️ ELIMINAR REGISTROS DE UN MES Y AÑO ESPECÍFICO (usando rango de fechas)
        @Modifying
        @Transactional
        @Query("DELETE FROM Registro r WHERE r.fecha >= :fechaInicio AND r.fecha <= :fechaFin")
        int deleteByMesYAnioRange(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

        // 📊 CONTAR REGISTROS DE UN MES Y AÑO (usando rango de fechas)
        @Query("SELECT COUNT(r) FROM Registro r WHERE r.fecha >= :fechaInicio AND r.fecha <= :fechaFin")
        long countByMesYAnioRange(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

        // 📅 OBTENER EL MES MÁS ANTIGUO CON REGISTROS
        @Query("SELECT MIN(r.fecha) FROM Registro r")
        LocalDate findFechaMasAntigua();
}
