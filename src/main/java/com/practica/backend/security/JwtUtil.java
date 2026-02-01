package com.practica.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "12345678901234567890123456789012";
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 horas

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
}}}
