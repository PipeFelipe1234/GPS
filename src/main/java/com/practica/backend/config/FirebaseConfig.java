package com.practica.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    static {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;

                // 🔹 OPCIÓN 1: Variable de entorno FIREBASE_CONFIG (para Railway/Producción)
                String firebaseConfig = System.getenv("FIREBASE_CONFIG");
                if (firebaseConfig != null && !firebaseConfig.isEmpty()) {
                    serviceAccount = new ByteArrayInputStream(firebaseConfig.getBytes(StandardCharsets.UTF_8));
                    System.out.println("✅ Firebase: Usando credenciales desde variable de entorno");
                } else {
                    // 🔹 OPCIÓN 2: Archivo local firebase-key.json (para desarrollo local)
                    try {
                        serviceAccount = new FileInputStream("firebase-key.json");
                        System.out.println("✅ Firebase: Usando archivo firebase-key.json local");
                    } catch (Exception e) {
                        System.out.println("⚠️  Firebase: No se encontró firebase-key.json ni FIREBASE_CONFIG");
                    }
                }

                if (serviceAccount != null) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    FirebaseApp.initializeApp(options);
                    System.out.println("✅ Firebase inicializado correctamente");
                } else {
                    System.out.println("❌ Firebase NO inicializado - Las notificaciones push no funcionarán");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
