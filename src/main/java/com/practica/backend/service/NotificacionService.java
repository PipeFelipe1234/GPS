package com.practica.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.BatchResponse;
import com.practica.backend.entity.TokenDispositivo;
import com.practica.backend.entity.Usuario;
import com.practica.backend.repository.TokenDispositivoRepository;
import org.springframework.stereotype.Service;
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
        List<TokenDispositivo> tokensAdmins = tokenDispositivoRepository.findTokensActivosAdmins();

        if (tokensAdmins.isEmpty()) {
            System.out.println("⚠️  No hay ADMINs con dispositivos registrados");
            return;
        }

        List<String> tokenList = tokensAdmins.stream()
                .map(TokenDispositivo::getToken)
                .toList();

        enviarNotificacionAMultiplesDispositivos(tokenList, titulo, mensaje, datos);
    }

    /**
     * 📲 Envía notificación a múltiples dispositivos
     */
    private void enviarNotificacionAMultiplesDispositivos(
            List<String> tokens,
            String titulo,
            String mensaje,
            Map<String, String> datos) {
        try {
            if (tokens.isEmpty()) {
                return;
            }

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .putAllData(datos)
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(titulo)
                                    .setBody(mensaje)
                                    .build())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            System.out.println("✅ Notificaciones enviadas: " + response.getSuccessCount() +
                    " exitosas, " + response.getFailureCount() + " fallidas");

            // Procesar tokens fallidos
            if (response.getFailureCount() > 0) {
                procesarTokensFallidos(response, tokens);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al enviar notificaciones: " + e.getMessage());
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
            // Verificar si el token ya existe
            if (tokenDispositivoRepository.findByToken(token).isPresent()) {
                System.out.println("ℹ️  El token ya está registrado");
                return;
            }

            TokenDispositivo nuevoToken = new TokenDispositivo(usuario, token, tipoDispositivo, marca, modelo);
            tokenDispositivoRepository.save(nuevoToken);
            System.out.println("✅ Token de dispositivo registrado: " + token.substring(0, 20) + "...");
        } catch (Exception e) {
            System.err.println("❌ Error al registrar token: " + e.getMessage());
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
