# 📲 Firebase Cloud Messaging (FCM) - Guía de Implementación

## 📋 Resumen

Se ha implementado un sistema completo de **notificaciones push con Firebase** para tu aplicación de asistencia. Cada vez que un usuario marque entrada o salida, los ADMINs recibirán una notificación en tiempo real en sus dispositivos (Android, iOS, Web).

---

## 🔧 Configuración Inicial (IMPORTANTE)

### 1️⃣ Obtener las credenciales de Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a **Project Settings** → **Service Accounts**
4. Haz click en **Generate new private key**
5. Se descargará un archivo JSON (ej: `practica-backend-firebase-adminsdk-xxxxx.json`)

### 2️⃣ Agregar el archivo a tu proyecto

1. Copia el archivo descargado a la **raíz del proyecto backend**
2. Renómbralo a `firebase-key.json`
3. ⚠️ **IMPORTANTE**: Agregarlo a `.gitignore` para no exponerlo

```bash
# .gitignore
firebase-key.json
```

### 3️⃣ La aplicación iniciará automáticamente

Cuando el backend arranque, verá el archivo `firebase-key.json` e inicializará Firebase automáticamente.

---

## 📱 Flujo de Funcionamiento

### Cliente (Flutter Frontend)

1. **Al iniciar sesión**: El cliente obtiene el token FCM del dispositivo
2. **Registrar token**: Envía el token al backend
3. **Recibir notificaciones**: Cuando ocurra entrada/salida, recibirá la notificación

### Backend (Spring Boot)

1. **Registrar token**: Almacena el token en la base de datos
2. **Entrada/Salida**: Al guardar registro, envía notificación a ADMINs
3. **Gestión de tokens**: Desactiva tokens inválidos automáticamente

---

## 🔌 Endpoints de la API

### 1️⃣ Registrar Token de Dispositivo

**POST** `/api/notificaciones/registrar-token`

**Headers:**

```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Body:**

```json
{
  "token": "FIREBASE_FCM_TOKEN_AQUÍ",
  "tipoDispositivo": "Android",
  "marca": "Samsung",
  "modelo": "Galaxy S21"
}
```

**Ejemplo en Postman:**

```
POST http://localhost:8080/api/notificaciones/registrar-token
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "token": "c1K2L3M4N5O6P7Q8R9S0T1U2V3W4X5Y6Z7A8B9C0D1E2F3",
  "tipoDispositivo": "Android",
  "marca": "Samsung",
  "modelo": "Galaxy A50"
}
```

**Respuesta (Exitosa):**

```json
{
  "mensaje": "✅ Token registrado correctamente",
  "exito": true
}
```

---

### 2️⃣ Desactivar Token de Dispositivo

**POST** `/api/notificaciones/desactivar-token`

**Body:**

```json
{
  "token": "FIREBASE_FCM_TOKEN_AQUÍ"
}
```

**Ejemplo en Postman:**

```
POST http://localhost:8080/api/notificaciones/desactivar-token
Content-Type: application/json

{
  "token": "c1K2L3M4N5O6P7Q8R9S0T1U2V3W4X5Y6Z7A8B9C0D1E2F3"
}
```

---

## 📤 Flujo de Notificaciones

### ✅ Cuando se marca ENTRADA

1. Usuario marca entrada (POST `/api/usuario/marcar-entrada`)
2. Backend guarda el registro
3. **Backend envía automáticamente notificación a todos los ADMINs:**
   - **Título:** ✅ Entrada Registrada
   - **Mensaje:** "Juan Pérez marcó entrada a las 08:30:00"
   - **Datos adicionales:**
     - `tipo: ENTRADA`
     - `registroId: 123`
     - `usuarioId: 45`
     - `fecha: 2026-02-06`
     - `hora: 08:30:00`

### 🚪 Cuando se marca SALIDA

1. Usuario marca salida (POST `/api/usuario/marcar-salida`)
2. Backend guarda el registro actualizado
3. **Backend envía automáticamente notificación a todos los ADMINs:**
   - **Título:** 🚪 Salida Registrada
   - **Mensaje:** "Juan Pérez marcó salida a las 17:30:00"
   - **Datos adicionales:**
     - `tipo: SALIDA`
     - `registroId: 123`
     - `usuarioId: 45`
     - `fecha: 2026-02-06`
     - `hora: 17:30:00`

---

## 📱 Implementación en Flutter (Frontend)

### Instalación de Firebase Messaging

1. Agrega a `pubspec.yaml`:

```yaml
dependencies:
  firebase_core: ^2.24.0
  firebase_messaging: ^14.6.0
  flutter_local_notifications: ^15.1.5
```

### Código Flutter para registrar token

```dart
import 'package:firebase_messaging/firebase_messaging.dart';

class NotificacionManager {
  static final FirebaseMessaging _firebaseMessaging = FirebaseMessaging.instance;

  // Inicializar FCM
  static Future<void> inicializar() async {
    // Solicitar permiso al usuario
    NotificationSettings settings = await _firebaseMessaging.requestPermission(
      alert: true,
      announcement: false,
      badge: true,
      carryForward: true,
      critical: false,
      provisional: false,
      sound: true,
    );

    if (settings.authorizationStatus == AuthorizationStatus.authorized) {
      print('Permiso de notificación concedido');
      await registrarToken();
    }
  }

  // Registrar token en el backend
  static Future<void> registrarToken() async {
    String? token = await _firebaseMessaging.getToken();

    if (token != null) {
      // Obtener datos del dispositivo
      String tipoDispositivo = "Android"; // o "iOS"
      String marca = "Samsung"; // Obtener dinámicamente
      String modelo = "Galaxy S21"; // Obtener dinámicamente

      // Enviar al backend
      await http.post(
        Uri.parse('http://TU_IP:8080/api/notificaciones/registrar-token'),
        headers: {
          'Authorization': 'Bearer $token_jwt',
          'Content-Type': 'application/json',
        },
        body: json.encode({
          'token': token,
          'tipoDispositivo': tipoDispositivo,
          'marca': marca,
          'modelo': modelo,
        }),
      );
    }
  }

  // Escuchar notificaciones en foreground
  static void escucharNotificaciones() {
    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      print('Notificación recibida:');
      print('Título: ${message.notification?.title}');
      print('Mensaje: ${message.notification?.body}');
      print('Datos: ${message.data}');

      // Aquí puedes mostrar la notificación localmente
      _mostrarNotificacionLocal(message);
    });
  }

  // Escuchar cuando el usuario hace click en la notificación
  static void escucharClicksNotificaciones() {
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      print('Notificación clickeada');
      print('Tipo: ${message.data['tipo']}');

      // Navegar a la pantalla de registros, por ejemplo
      if (message.data['tipo'] == 'ENTRADA' || message.data['tipo'] == 'SALIDA') {
        // Navigator.of(context).pushNamed('/registros');
      }
    });
  }

  static void _mostrarNotificacionLocal(RemoteMessage message) {
    // Aquí integrar con flutter_local_notifications para mostrar
    // notificaciones bonitas con sonido y vibración
  }
}
```

### En main.dart:

```dart
void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Inicializar Firebase
  await Firebase.initializeApp();

  // Inicializar notificaciones
  await NotificacionManager.inicializar();
  NotificacionManager.escucharNotificaciones();
  NotificacionManager.escucharClicksNotificaciones();

  runApp(const MyApp());
}
```

---

## 🗄️ Estructura de Base de Datos

Se agregó una nueva tabla: `tokens_dispositivos`

```sql
CREATE TABLE tokens_dispositivos (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  token VARCHAR(500) UNIQUE NOT NULL,
  tipo_dispositivo VARCHAR(50) NOT NULL,
  marca VARCHAR(100),
  modelo VARCHAR(100),
  fecha_registro DATETIME NOT NULL,
  ultima_actividad DATETIME,
  activo BOOLEAN DEFAULT true,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
```

---

## 📊 Clases Creadas

1. **Entity:** `TokenDispositivo.java`
   - Almacena tokens de dispositivos registrados

2. **DTO:**
   - `TokenDispositivoRequest.java` - Para registrar tokens
3. **Repository:** `TokenDispositivoRepository.java`
   - Métodos para buscar tokens activos
   - Búsqueda por usuario y por rol ADMIN

4. **Service:** `NotificacionService.java`
   - Envía notificaciones a dispositivos específicos
   - Envía a múltiples dispositivos
   - Envía a todos los ADMINs
   - Gestiona tokens fallidos

5. **Controller:** `NotificacionController.java`
   - Endpoints para registrar y desactivar tokens

6. **Config:** `FirebaseConfig.java`
   - Inicializa Firebase automáticamente

---

## ⚙️ Modificaciones a Clases Existentes

### `RegistroService.java`

- Ahora inyecta `NotificacionService`
- Llama a `enviarNotificacionEntrada()` al marcar entrada
- Llama a `enviarNotificacionSalida()` al marcar salida

### `UsuarioService.java`

- Método `obtenerPorIdentificacion()` retorna `null` si no existe

### `pom.xml`

- Agregada dependencia: `firebase-admin:9.2.0`

---

## 🧪 Pruebas en Postman

### 1️⃣ Login y obtener JWT

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "identificacion": "123456"
}
```

Guarda el token retornado.

### 2️⃣ Registrar token de dispositivo

```
POST http://localhost:8080/api/notificaciones/registrar-token
Authorization: Bearer <TOKEN_DEL_PASO_1>
Content-Type: application/json

{
  "token": "dN2JF3K4L5M6N7O8P9Q0R1S2T3U4V5W6X7Y8Z9A0B1C2",
  "tipoDispositivo": "Android",
  "marca": "Samsung",
  "modelo": "Galaxy A50"
}
```

### 3️⃣ Marcar entrada

```
POST http://localhost:8080/api/usuario/marcar-entrada
Authorization: Bearer <TOKEN_DEL_PASO_1>
Content-Type: application/json

{
  "latitudCheckin": 4.7110,
  "longitudCheckin": -74.0721,
  "precisionMetrosCheckin": 10.5
}
```

**Resultado:** Los ADMINs recibirán una notificación push automáticamente.

---

## 🔐 Notas de Seguridad

✅ El archivo `firebase-key.json` NO debe subirse a Git  
✅ Los tokens son únicos para cada dispositivo  
✅ Solo se envían notificaciones a ADMINs  
✅ Los tokens inválidos se desactivan automáticamente  
✅ El JWT es requerido para registrar tokens

---

## 🐛 Solución de Problemas

### "Firebase no está inicializado"

→ Asegúrate de que `firebase-key.json` está en la raíz del proyecto

### "Token inválido al registrar"

→ Verifica que el JWT sea válido y que el usuario exista

### "No recibo notificaciones"

→ Verifica que el usuario sea ADMIN
→ Revisa que el token FCM sea válido
→ Comprueba en Firebase Console → Cloud Messaging

### "Error de compilación deprecated API"

→ Es una advertencia de Firebase SDK, no afecta la funcionalidad

---

## 📞 Contacto

Si tienes dudas sobre la implementación, revisa:

- [Firebase Admin SDK Documentation](https://firebase.google.com/docs/admin/setup)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [FlutterFire Documentation](https://firebase.flutter.dev/)
