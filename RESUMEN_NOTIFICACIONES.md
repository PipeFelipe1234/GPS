# ✅ Notificaciones Push Firebase - Resumen de Implementación

## 🎯 ¿Qué se implementó?

Se ha creado un **sistema completo de notificaciones push con Firebase Cloud Messaging (FCM)** que permite:

✅ **Notificaciones automáticas para ADMIN** cada vez que un usuario marca entrada o salida  
✅ **Almacenamiento seguro de tokens** de dispositivos en base de datos  
✅ **Soporte para Android, iOS y Web**  
✅ **Gestión automática** de tokens fallidos  
✅ **API REST para registrar y desactivar tokens**

---

## 📦 Archivos Creados

### **Entidades** (Entity)

```
src/main/java/com/practica/backend/entity/
└── TokenDispositivo.java          ← Almacena tokens de dispositivos
```

### **DTOs** (Transfer Objects)

```
src/main/java/com/practica/backend/dto/
└── TokenDispositivoRequest.java   ← Para registrar tokens
```

### **Repositorios** (Data Access)

```
src/main/java/com/practica/backend/repository/
└── TokenDispositivoRepository.java ← Métodos para buscar tokens
```

### **Servicios** (Business Logic)

```
src/main/java/com/practica/backend/service/
├── NotificacionService.java       ← Maneja notificaciones FCM
└── RegistroService.java           ← MODIFICADO (ahora envía notificaciones)
```

### **Controladores** (API Endpoints)

```
src/main/java/com/practica/backend/controller/
└── NotificacionController.java    ← Endpoints para tokens
```

### **Configuración** (Setup)

```
src/main/java/com/practica/backend/config/
└── FirebaseConfig.java            ← Inicializa Firebase automáticamente
```

### **Documentación**

```
├── NOTIFICACIONES_FCM.md          ← Guía completa de notificaciones
├── FLUTTER_FCM_GUIDE.md           ← Guía para Flutter frontend
└── FILTROS_ADMIN.md               ← Guía de filtros (anterior)
```

---

## 🔄 Flujo de Funcionamiento

```
┌─────────────────────────────────────────────────────────────────┐
│                      FLUJO DE NOTIFICACIONES                    │
└─────────────────────────────────────────────────────────────────┘

1. USUARIO FLUTTER (Frontend)
   ├─ Loguea
   ├─ Obtiene Token FCM desde Firebase
   └─ Registra Token en Backend → POST /api/notificaciones/registrar-token

2. BACKEND (Spring Boot)
   ├─ Recibe Token FCM
   ├─ Guarda en BD (tabla tokens_dispositivos)
   └─ Responde OK

3. USUARIO MARCA ENTRADA/SALIDA
   ├─ Llamada API → POST /api/usuario/marcar-entrada
   └─ POST /api/usuario/marcar-salida

4. BACKEND GUARDA REGISTRO
   ├─ Crea registro en BD
   ├─ Llama a NotificacionService.enviarNotificacionAAdmins()
   └─ Envía notificación a todos los ADMINs

5. FIREBASE CLOUD MESSAGING
   ├─ Recibe solicitud del Backend
   ├─ Entrega notificación a dispositivos registrados
   └─ ADMINs reciben notificación push en tiempo real

6. ADMIN RECIBE NOTIFICACIÓN
   ├─ Título: ✅ Entrada Registrada / 🚪 Salida Registrada
   ├─ Mensaje: "Juan Pérez marcó entrada a las 08:30:00"
   └─ Datos: Tipo, ID registro, usuario, fecha, hora
```

---

## 🌐 Endpoints de la API

### **Registrar Token de Dispositivo**

```
POST /api/notificaciones/registrar-token
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "token": "FIREBASE_TOKEN",
  "tipoDispositivo": "Android",
  "marca": "Samsung",
  "modelo": "Galaxy S21"
}

Response: { "mensaje": "✅ Token registrado correctamente", "exito": true }
```

### **Desactivar Token**

```
POST /api/notificaciones/desactivar-token
Content-Type: application/json

{
  "token": "FIREBASE_TOKEN"
}

Response: { "mensaje": "✅ Token desactivado correctamente", "exito": true }
```

---

## 📊 Base de Datos

### Nueva tabla: `tokens_dispositivos`

```sql
┌─────────────────────────────────────────────────────────────┐
│              tokens_dispositivos                            │
├─────────────────────────────────────────────────────────────┤
│ id (BIGINT PK)                                              │
│ usuario_id (BIGINT FK) → usuarios.id                       │
│ token (VARCHAR 500) UNIQUE                                 │
│ tipo_dispositivo (VARCHAR 50) - Android, iOS, Web         │
│ marca (VARCHAR 100) - Samsung, Apple, etc                 │
│ modelo (VARCHAR 100) - Galaxy S21, iPhone 14, etc         │
│ fecha_registro (DATETIME)                                  │
│ ultima_actividad (DATETIME)                                │
│ activo (BOOLEAN) - true/false                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔐 Dependencias Agregadas (pom.xml)

```xml
<!-- Firebase Admin SDK para enviar notificaciones -->
<dependency>
  <groupId>com.google.firebase</groupId>
  <artifactId>firebase-admin</artifactId>
  <version>9.2.0</version>
</dependency>
```

---

## 🚀 Pasos para Empezar

### **Backend (Ya completado)**

✅ Firebase Admin SDK agregado a pom.xml  
✅ Entidades, DTOs y repositorios creados  
✅ Servicios y controladores implementados  
✅ Notificaciones automáticas en entrada/salida  
✅ Gestión de tokens integrada

### **Próximos pasos: Frontend (Flutter)**

1. **Instalar dependencias:**

   ```bash
   flutter pub add firebase_core firebase_messaging flutter_local_notifications device_info_plus
   ```

2. **Configurar Firebase en Flutter:**

   ```bash
   flutterfire configure
   ```

3. **Obtener credenciales Firebase:**
   - Ir a [Firebase Console](https://console.firebase.google.com)
   - Descargar `google-services.json` (Android) o `GoogleService-Info.plist` (iOS)

4. **Implementar código Flutter:**
   - Copiar código de `FLUTTER_FCM_GUIDE.md`
   - Registrar token al login
   - Desactivar token al logout

5. **Pruebas:**
   - Usar Postman para marcar entrada/salida
   - Ver notificaciones en tu dispositivo Flutter

---

## ✅ Checklist de Implementación

### Backend

- [x] Firebase Admin SDK agregado
- [x] Entidad TokenDispositivo creada
- [x] DTO TokenDispositivoRequest creado
- [x] Repository TokenDispositivoRepository creado
- [x] Service NotificacionService creado
- [x] Controller NotificacionController creado
- [x] Config FirebaseConfig creado
- [x] RegistroService modificado para enviar notificaciones
- [x] UsuarioService modificado
- [x] Proyecto compila sin errores ✅

### Frontend (Por hacer)

- [ ] Firebase inicializado en Flutter
- [ ] Token FCM registrado en backend
- [ ] Notificaciones locales configuradas
- [ ] Listeners para mensajes en foreground/background
- [ ] Click handlers para abrir registros
- [ ] Logout desactiva token

### Documentación (Completado)

- [x] NOTIFICACIONES_FCM.md - Guía completa
- [x] FLUTTER_FCM_GUIDE.md - Código Flutter listo para copiar
- [x] Este archivo - Resumen general

---

## 📝 Notas Importantes

⚠️ **firebase-key.json**

- Descargar de Firebase Console → Project Settings → Service Accounts
- Guardar en raíz del proyecto
- Agregar a `.gitignore` (⚠️ NO subir a Git)

⚠️ **JWT Token**

- El usuario debe estar logeado para registrar token
- El JWT debe ser válido y no estar expirado

⚠️ **ADMINs**

- Solo los usuarios con rol = 'ADMIN' reciben notificaciones
- Asegurate que existan usuarios ADMIN en la BD

⚠️ **IP del Backend**

- En Flutter, cambiar la IP según tu red local
- No usar `localhost:8080` desde dispositivo móvil

---

## 🧪 Prueba Rápida

### 1. Inicia el backend

```bash
./mvnw.cmd spring-boot:run
```

### 2. En Postman:

**a) Login:**

```
POST http://localhost:8080/api/auth/login
{
  "identificacion": "123456"  // Usuario ADMIN
}
→ Copiar token del response
```

**b) Registrar token (desde Flutter/Postman):**

```
POST http://localhost:8080/api/notificaciones/registrar-token
Authorization: Bearer <TOKEN_DEL_LOGIN>
{
  "token": "dN2JF3K4L5M6N7O8P9Q0R1S2T3U4V5W6X7Y8Z9A0B1C2",
  "tipoDispositivo": "Android",
  "marca": "Samsung",
  "modelo": "Galaxy A50"
}
→ Deberías ver ✅ Token registrado correctamente
```

**c) Marcar entrada (desde Flutter/Postman):**

```
POST http://localhost:8080/api/usuario/marcar-entrada
Authorization: Bearer <JWT_DE_USUARIO>
{
  "latitudCheckin": 4.7110,
  "longitudCheckin": -74.0721,
  "precisionMetrosCheckin": 10.5
}
→ ADMIN recibirá notificación push automáticamente 🎉
```

---

## 📚 Documentación Referencia

- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Flutter Firebase Integration](https://firebase.flutter.dev/)
- [Cloud Messaging API](https://firebase.google.com/docs/cloud-messaging/concept-options)

---

## 🎉 ¡Listo!

El backend está completamente implementado y listo para recibir notificaciones. Ahora solo falta integrar Firebase en tu aplicación Flutter siguiendo la guía `FLUTTER_FCM_GUIDE.md`.

¿Preguntas? Revisa los archivos `.md` generados o contacta al equipo de desarrollo.

**Status:** ✅ Backend Completado | ⏳ Frontend Pendiente
