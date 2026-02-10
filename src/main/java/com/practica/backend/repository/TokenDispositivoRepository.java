package com.practica.backend.repository;

import com.practica.backend.entity.TokenDispositivo;
import com.practica.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TokenDispositivoRepository extends JpaRepository<TokenDispositivo, Long> {

    Optional<TokenDispositivo> findByToken(String token);

    List<TokenDispositivo> findByUsuario(Usuario usuario);

    @Query("SELECT t FROM TokenDispositivo t WHERE t.usuario = :usuario AND t.activo = true")
    List<TokenDispositivo> findTokensActivosByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT t FROM TokenDispositivo t WHERE t.usuario.rol = 'ADMIN' AND t.activo = true")
    List<TokenDispositivo> findTokensActivosAdmins();

    // 📲 Obtener todos los tokens activos (para notificaciones masivas)
    List<TokenDispositivo> findByActivoTrue();
}
