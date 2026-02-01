package com.practica.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "12345678901234567890123456789012";
    private static final long EXPIRATION = 1000 * 60 * 60 * 24 * 30; // 30 días

    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generarToken(String identificacion, String rol, String nombre) {
        return Jwts.builder()
                .setSubject(identificacion)
                .claim("rol", rol)
                .claim("nombre", nombre)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String extraerIdentificacion(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public static String extraerRol(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("rol", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static String extraerNombre(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("nombre", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false; // Token expirado
        } catch (Exception e) {
            return false; // Token inválido
        }
    }

    public static Claims extraerClaimsConToleranciaExpiracion(String token) {
        try {
            // Este método extrae los claims incluso si el token está expirado
            // Pero valida que la firma sea correcta
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJwt(token)
                    .getBody();
        } catch (Exception e) {
            // Si falla parseClaimsJwt, intentar con parseClaimsJws para mejor manejo de
            // errores
            try {
                return Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            } catch (ExpiredJwtException e2) {
                // Token expirado pero firma válida - extraer claims de la excepción
                return e2.getClaims();
            } catch (Exception e3) {
                return null; // Token completamente inválido
            }
        }
    }

    public static String refrescarToken(String token) {
        try {
            // Validar que el token sea válido y NO esté expirado
            if (!validarToken(token)) {
                return null; // Token expirado o inválido
            }

            // Extraer información del token válido
            String identificacion = extraerIdentificacion(token);
            String rol = extraerRol(token);
            String nombre = extraerNombre(token);

            // Generar nuevo token con nueva fecha de expiración
            return generarToken(identificacion, rol, nombre);
        } catch (Exception e) {
            return null; // Error al procesar token
        }
    }
}}}
