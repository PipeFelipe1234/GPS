package com.practica.backend.repository;

import com.practica.backend.entity.Registro;
import com.practica.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface RegistroRepository extends JpaRepository<Registro, Long> {

    Optional<Registro> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);

    Optional<Registro> findByUsuarioAndFechaAndHoraSalidaIsNull(Usuario usuario, LocalDate fecha);

    List<Registro> findAllByUsuario(Usuario usuario);

    // 🔍 FILTROS PERSONALIZADOS PARA ADMIN
    @Query("SELECT r FROM Registro r JOIN r.usuario u WHERE " +
            "(:fecha IS NULL OR r.fecha = :fecha) AND " +
            "(:identificacion IS NULL OR LOWER(u.identificacion) LIKE LOWER(CONCAT('%', :identificacion, '%'))) AND " +
            "(:nombres IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombres, '%')))")
    List<Registro> findByFiltros(
            @Param("fecha") LocalDate fecha,
            @Param("identificacion") String identificacion,
            @Param("nombres") String nombres);
}
