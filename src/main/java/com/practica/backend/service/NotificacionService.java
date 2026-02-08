package com.practica.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.SendResponse;
import com.practica.backend.entity.TokenDispositivo;
import com.practica.backend.entity.Usuario;
import com.practica.backend.repository.TokenDispositivoRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotificacionService {

    private final TokenDispositivoRepository tokenDispositivoRepository;

    public NotificacionService(TokenDispositivoRepository tokenDispositivoRepository) {
        this.tokenDispositivoRepository = tokenDispositivoRepository;
    }

    /**
     * 📲 Envía notificación a un dispositivo específico
     */
    public void enviarNotificacionADispositivo(
            String token,
            String titulo,
            String mensaje,
            Map<String, String> datos) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .putAllData(datos)
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(titulo)
                                    .setBody(mensaje)
                                    .build())
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Notificación enviada correctamente: " + messageId);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar notificación: " + e.getMessage());
        }
    }

    /**
     * 📲 Envía notificación a múltiples dispositivos (a un usuario específico)
     */
    public void enviarNotificacionAUsuario(
            Usuario usuario,
            String titulo,
            String mensaje,
            Map<String, String> datos) {
        List<TokenDispositivo> tokens = tokenDispositivoRepository.findTokensActivosByUsuario(usuario);

        if (tokens.isEmpty()) {
            System.out.println("⚠️  El usuario " + usuario.getNombre() + " no tiene dispositivos registrados");
            return;
        }

        List<String> tokenList = tokens.stream()
                .map(TokenDispositivo::getToken)
                .toList();

        enviarNotificacionAMultiplesDispositivos(tokenList, titulo, mensaje, datos);
    }

    /**
     * 📲 Envía notificación a todos los ADMIN
     */
    public void enviarNotificacionAAdmins(
            String titulo,
            String mensaje,
            Map<String, String> datos) {
        System.out.println("📤 Buscando tokens de ADMINs para enviar notificación...");
        System.out.println("   Título: " + titulo);
        System.out.println("   Mensaje: " + mensaje);

        List<TokenDispositivo> tokensAdmins = tokenDispositivoRepository.findTokensActivosAdmins();

        if (tokensAdmins.isEmpty()) {
            System.out.println("⚠️  No hay ADMINs con dispositivos registrados");
            return;
        }

        System.out.println("📱 Encontrados " + tokensAdmins.size() + " dispositivo(s) de ADMIN");

        List<String> tokenList = tokensAdmins.stream()
                .map(TokenDispositivo::getToken)
                .toList();

        // Log de tokens (parcial por seguridad)
        for (int i = 0; i < tokenList.size(); i++) {
            String token = tokenList.get(i);
            System.out.println("   Token " + (i + 1) + ": " + token.substring(0, Math.min(30, token.length())) + "...");
        }

        enviarNotificacionAMultiplesDispositivos(tokenList, titulo, mensaje, datos);
    }

    /**
     * 📲 Envía notificación a múltiples dispositivos (usando sendEach - API v1)
     */
    private void enviarNotificacionAMultiplesDispositivos(
            List<String> tokens,
            String titulo,
            String mensaje,
            Map<String, String> datos) {
        try {
            if (tokens.isEmpty()) {
                System.out.println("⚠️  Lista de tokens vacía, no se envían notificaciones");
                return;
            }

            System.out.println("📤 Enviando notificación a " + tokens.size() + " dispositivo(s)...");

            // Crear lista de mensajes individuales (API HTTP v1)
            List<Message> messages = new ArrayList<>();
            for (String token : tokens) {
                Message msg = Message.builder()
                        .setToken(token)
                        .putAllData(datos)
                        .setNotification(
                                com.google.firebase.messaging.Notification.builder()
                                        .setTitle(titulo)
                                        .setBody(mensaje)
                                        .build())
                        .build();
                messages.add(msg);
            }

            // Usar sendEach en lugar de sendMulticast (API v1)
            BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages);

            System.out.println("✅ Notificaciones enviadas: " + response.getSuccessCount() +
                    " exitosas, " + response.getFailureCount() + " fallidas");

            // Procesar tokens fallidos
            if (response.getFailureCount() > 0) {
                procesarTokensFallidos(response, tokens);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al enviar notificaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🗑️ Procesa tokens fallidos y los marca como inactivos
     */
    private void procesarTokensFallidos(BatchResponse response, List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (!response.getResponses().get(i).isSuccessful()) {
                String token = tokens.get(i);
                tokenDispositivoRepository.findByToken(token).ifPresent(td -> {
                    td.setActivo(false);
                    tokenDispositivoRepository.save(td);
                    System.out.println("🗑️  Token inactivado: " + token);
                });
            }
        }
    }

    /**
     * ✅ Registra un nuevo token de dispositivo
     */
    public void registrarTokenDispositivo(Usuario usuario, String token, String tipoDispositivo,
            String marca, String modelo) {
        try {
            System.out.println("📱 Intentando registrar token FCM para usuario: " + usuario.getNombre() + " (Rol: "
                    + usuario.getRol() + ")");
            System.out.println(
                    "   Token: " + (token != null ? token.substring(0, Math.min(30, token.length())) + "..." : "NULL"));
            System.out.println("   Tipo: " + tipoDispositivo + ", Marca: " + marca + ", Modelo: " + modelo);

            if (token == null || token.isEmpty()) {
                System.err.println("❌ El token FCM está vacío o es null");
                return;
            }

            // Verificar si el token ya existe
            var existente = tokenDispositivoRepository.findByToken(token);
            if (existente.isPresent()) {
                // Actualizar última actividad del token existente
                TokenDispositivo td = existente.get();
                td.setUltimaActividad(java.time.LocalDateTime.now());
                td.setActivo(true);
                tokenDispositivoRepository.save(td);
                System.out.println("ℹ️  Token ya existía, actualizada última actividad");
                return;
            }

            TokenDispositivo nuevoToken = new TokenDispositivo(usuario, token, tipoDispositivo, marca, modelo);
            tokenDispositivoRepository.save(nuevoToken);
            System.out.println("✅ Token FCM registrado exitosamente para: " + usuario.getNombre());

            // Mostrar cuántos tokens de admin hay ahora
            long totalAdmins = tokenDispositivoRepository.findTokensActivosAdmins().size();
            System.out.println("📊 Total tokens de ADMINs activos: " + totalAdmins);
        } catch (Exception e) {
            System.err.println("❌ Error al registrar token: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ❌ Desactiva un token
     */
    public void desactivarToken(String token) {
        tokenDispositivoRepository.findByToken(token).ifPresent(td -> {
            td.setActivo(false);
            tokenDispositivoRepository.save(td);
            System.out.println("✅ Token desactivado: " + token.substring(0, 20) + "...");
        });
    }
}
