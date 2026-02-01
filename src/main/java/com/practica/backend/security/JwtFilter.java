package com.practica.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String requestUri = request.getRequestURI();

        // Permitir que el endpoint de refresh procese tokens expirados
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            try {
                String identificacion = JwtUtil.extraerIdentificacion(token);
                String rol = JwtUtil.extraerRol(token);

                // Crear lista de autoridades
                List<SimpleGrantedAuthority> autoridades = new ArrayList<>();
                if (rol != null && !rol.isEmpty()) {
                    autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol));
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        identificacion,
                        null,
                        autoridades);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                // Si es el endpoint de refresh, permitir que continúe con token expirado
                if (requestUri.contains("/api/auth/refresh")) {
                    // Extraer información del token expirado para el endpoint
                    try {
                        String identificacion = e.getClaims().getSubject();
                        String rol = e.getClaims().get("rol", String.class);

                        List<SimpleGrantedAuthority> autoridades = new ArrayList<>();
                        if (rol != null && !rol.isEmpty()) {
                            autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol));
                        }

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                identificacion,
                                null,
                                autoridades);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } catch (Exception ex) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                } else {
                    // Para otros endpoints, rechazar token expirado
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                // Token inválido
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
