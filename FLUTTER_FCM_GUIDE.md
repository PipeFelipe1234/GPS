# 🚀 Obtener Token FCM en Flutter - Guía Paso a Paso

## 1️⃣ Instalación de Dependencias

Agrega a tu `pubspec.yaml`:

```yaml
dependencies:
  flutter:
    sdk: flutter
  firebase_core: ^2.24.0
  firebase_messaging: ^14.6.0
  flutter_local_notifications: ^15.1.5
  device_info_plus: ^9.1.0 # Para obtener marca y modelo del dispositivo
```

Luego ejecuta:

```bash
flutter pub get
```

---

## 2️⃣ Configuración en Firebase Console

### Para Android:

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. En **Project Settings** → **Cloud Messaging** → **Android Configuration**
4. Copia tu **Server API Key** (la necesitarás para pruebas)

### Para iOS:

1. Ve a **Cloud Messaging** → **APNs Configuration**
2. Sube tu certificado APNs

---

## 3️⃣ Código Flutter para Obtener y Registrar Token

### Crear archivo `lib/services/notificacion_service.dart`:

```dart
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:http/http.dart' as http;
import 'dart:io';
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class NotificacionService {
  static final FirebaseMessaging _firebaseMessaging = FirebaseMessaging.instance;
  static final FlutterLocalNotificationsPlugin _localNotifications =
      FlutterLocalNotificationsPlugin();
  static final DeviceInfoPlugin _deviceInfo = DeviceInfoPlugin();

  // URL del backend
  static const String _backendUrl = 'http://192.168.1.100:8080'; // Cambiar IP

  /// 🔧 Inicializar FCM y permisos
  static Future<void> inicializar() async {
    print('🔧 Inicializando Firebase Messaging...');

    // Solicitar permisos al usuario
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
      print('✅ Permiso de notificación concedido');

      // Obtener y registrar token
      await _obtenerYRegistrarToken();
    } else if (settings.authorizationStatus == AuthorizationStatus.provisional) {
      print('⚠️  Permiso provisional de notificación concedido');
      await _obtenerYRegistrarToken();
    } else {
      print('❌ Permiso de notificación denegado');
    }

    // Escuchar mensajes en foreground
    _escucharMensajesEnForeground();

    // Escuchar clics en notificaciones
    _escucharClicksNotificaciones();
  }

  /// 📱 Obtener token FCM y registrarlo en el backend
  static Future<void> _obtenerYRegistrarToken() async {
    try {
      // Obtener token FCM
      String? token = await _firebaseMessaging.getToken();

      if (token == null) {
        print('❌ No se pudo obtener el token FCM');
        return;
      }

      print('📱 Token FCM obtenido: ${token.substring(0, 30)}...');

      // Guardar token localmente
      SharedPreferences prefs = await SharedPreferences.getInstance();
      await prefs.setString('fcm_token', token);

      // Obtener información del dispositivo
      String tipoDispositivo = Platform.isAndroid ? 'Android' : 'iOS';
      String marca = await _obtenerMarcaDispositivo();
      String modelo = await _obtenerModeloDispositivo();

      print('📊 Dispositivo: $marca $modelo ($tipoDispositivo)');

      // Enviar token al backend
      await _registrarTokenEnBackend(
        token: token,
        tipoDispositivo: tipoDispositivo,
        marca: marca,
        modelo: modelo,
      );

    } catch (e) {
      print('❌ Error al obtener token: $e');
    }
  }

  /// 📊 Obtener marca del dispositivo
  static Future<String> _obtenerMarcaDispositivo() async {
    try {
      if (Platform.isAndroid) {
        AndroidDeviceInfo androidInfo = await _deviceInfo.androidInfo;
        return androidInfo.manufacturer ?? 'Unknown';
      } else if (Platform.isIOS) {
        return 'Apple';
      }
    } catch (e) {
      print('Error al obtener marca: $e');
    }
    return 'Unknown';
  }

  /// 📊 Obtener modelo del dispositivo
  static Future<String> _obtenerModeloDispositivo() async {
    try {
      if (Platform.isAndroid) {
        AndroidDeviceInfo androidInfo = await _deviceInfo.androidInfo;
        return androidInfo.model ?? 'Unknown';
      } else if (Platform.isIOS) {
        IosDeviceInfo iosInfo = await _deviceInfo.iosInfo;
        return iosInfo.model ?? 'Unknown';
      }
    } catch (e) {
      print('Error al obtener modelo: $e');
    }
    return 'Unknown';
  }

  /// 🔌 Registrar token en el backend
  static Future<void> _registrarTokenEnBackend({
    required String token,
    required String tipoDispositivo,
    required String marca,
    required String modelo,
  }) async {
    try {
      // Obtener JWT del almacenamiento local
      SharedPreferences prefs = await SharedPreferences.getInstance();
      String? jwtToken = prefs.getString('auth_token');

      if (jwtToken == null) {
        print('❌ No hay token JWT disponible. El usuario debe estar logeado.');
        return;
      }

      final response = await http.post(
        Uri.parse('$_backendUrl/api/notificaciones/registrar-token'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $jwtToken',
        },
        body: jsonEncode({
          'token': token,
          'tipoDispositivo': tipoDispositivo,
          'marca': marca,
          'modelo': modelo,
        }),
      ).timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        print('✅ Token registrado en el backend correctamente');
      } else {
        print('❌ Error al registrar token: ${response.statusCode}');
        print('Respuesta: ${response.body}');
      }
    } catch (e) {
      print('❌ Error de conexión: $e');
    }
  }

  /// 📲 Escuchar notificaciones en foreground
  static void _escucharMensajesEnForeground() {
    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      print('\n🔔 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      print('📬 Notificación recibida en FOREGROUND');
      print('📌 Título: ${message.notification?.title}');
      print('💬 Mensaje: ${message.notification?.body}');
      print('📊 Datos: ${message.data}');
      print('🔔 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');

      // Mostrar notificación local
      _mostrarNotificacionLocal(message);
    });
  }

  /// 🖱️ Escuchar clicks en notificaciones
  static void _escucharClicksNotificaciones() {
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      print('\n✋ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      print('👆 Usuario hizo CLICK en la notificación');
      print('📌 Tipo: ${message.data['tipo']}');
      print('✋ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');

      // Aquí puedes navegar a la pantalla correspondiente
      String tipo = message.data['tipo'] ?? '';
      if (tipo == 'ENTRADA' || tipo == 'SALIDA') {
        print('🚀 Navegando a pantalla de Registros...');
        // Navigator.of(context).pushNamed('/registros');
      }
    });

    // Para notificaciones que abren la app desde background
    FirebaseMessaging.instance.getInitialMessage().then((message) {
      if (message != null) {
        print('📬 Notificación inicial: ${message.data}');
      }
    });
  }

  /// 🔔 Mostrar notificación local
  static Future<void> _mostrarNotificacionLocal(RemoteMessage message) async {
    try {
      const AndroidNotificationChannel channel = AndroidNotificationChannel(
        'asistencia_channel',
        'Notificaciones de Asistencia',
        description: 'Canal para notificaciones de entrada y salida',
        importance: Importance.max,
        sound: RawResourceAndroidNotificationSound('notification'),
      );

      await _localNotifications
          .resolvePlatformSpecificImplementation<
              AndroidFlutterLocalNotificationsPlugin>()
          ?.createNotificationChannel(channel);

      RemoteNotification? notification = message.notification;

      if (notification != null) {
        _localNotifications.show(
          notification.hashCode,
          notification.title,
          notification.body,
          NotificationDetails(
            android: AndroidNotificationDetails(
              channel.id,
              channel.name,
              channelDescription: channel.description,
              sound: const RawResourceAndroidNotificationSound('notification'),
              importance: Importance.max,
              priority: Priority.high,
            ),
          ),
        );
      }
    } catch (e) {
      print('Error al mostrar notificación local: $e');
    }
  }

  /// 🔄 Escuchar cambios de token (se cambia ocasionalmente)
  static void _escucharCambiosDeToken() {
    _firebaseMessaging.onTokenRefresh.listen((newToken) {
      print('🔄 Token FCM refrescado: ${newToken.substring(0, 30)}...');
      // Aquí puedes registrar el nuevo token en tu backend
      // await _registrarTokenEnBackend(...);
    });
  }

  /// 📤 Desactivar token (cuando el usuario se logout)
  static Future<void> desactivarToken() async {
    try {
      SharedPreferences prefs = await SharedPreferences.getInstance();
      String? token = prefs.getString('fcm_token');
      String? jwtToken = prefs.getString('auth_token');

      if (token == null || jwtToken == null) {
        print('⚠️  No hay token para desactivar');
        return;
      }

      final response = await http.post(
        Uri.parse('$_backendUrl/api/notificaciones/desactivar-token'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'token': token,
        }),
      );

      if (response.statusCode == 200) {
        print('✅ Token desactivado correctamente');
      }
    } catch (e) {
      print('❌ Error al desactivar token: $e');
    }
  }
}
```

---

## 4️⃣ Inicializar en main.dart

```dart
import 'package:firebase_core/firebase_core.dart';
import 'firebase_options.dart'; // Se genera automáticamente
import 'services/notificacion_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Inicializar Firebase
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );

  // Inicializar notificaciones
  await NotificacionService.inicializar();

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Asistencia',
      home: const LoginScreen(),
      // ... resto de configuración
    );
  }
}
```

---

## 5️⃣ Llamar al logout para desactivar token

```dart
// En tu AuthService o LoginService
Future<void> logout() async {
  // Desactivar token de notificaciones
  await NotificacionService.desactivarToken();

  // Limpiar datos locales
  SharedPreferences prefs = await SharedPreferences.getInstance();
  await prefs.remove('auth_token');

  // Navegar a login
  // Navigator.of(context).pushReplacementNamed('/login');
}
```

---

## 6️⃣ Variables de Entorno

Crea un archivo `.env` o usa variables de entorno para la URL del backend:

```
BACKEND_URL=http://192.168.1.100:8080
```

Para usarlo con el paquete `flutter_dotenv`:

```dart
import 'package:flutter_dotenv/flutter_dotenv.dart';

static const String _backendUrl = String.fromEnvironment('BACKEND_URL',
    defaultValue: 'http://192.168.1.100:8080');
```

---

## 7️⃣ Prueba con el Emulador

### Android:

```bash
flutter run
```

### iOS:

```bash
flutter run -d macos  # Simulador
```

---

## 🧪 Prueba Manual

### 1. Ejecuta la app

```bash
flutter run
```

### 2. Loguéate

→ La app registrará automáticamente el token

### 3. En Postman, marca entrada/salida

```bash
POST http://localhost:8080/api/usuario/marcar-entrada
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "latitudCheckin": 4.7110,
  "longitudCheckin": -74.0721,
  "precisionMetrosCheckin": 10.5
}
```

### 4. ¡Verás la notificación en tu dispositivo! 🎉

---

## ⚠️ Notas Importantes

✅ **Permisos en AndroidManifest.xml:**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

✅ **Permisos en Info.plist (iOS):**

```xml
<key>NSUserNotificationAlertStyle</key>
<string>alert</string>
```

✅ **URL del backend:** Cambiar IP según tu red local

✅ **JWT:** Obtenerlo del login y guardarlo en SharedPreferences

---

## 🐛 Troubleshooting

| Problema                                | Solución                                            |
| --------------------------------------- | --------------------------------------------------- |
| "Token FCM es null"                     | Verifica que Google Play Services esté instalado    |
| "No recibo notificaciones"              | Comprueba que la app tenga permisos de notificación |
| "Error 401 al registrar token"          | Verifica que el JWT sea válido y no esté expirado   |
| "Error de conexión"                     | Asegúrate que la IP del backend sea correcta        |
| "App se cierra al recibir notificación" | Revisa los logs en AndroidStudio                    |

---

## 📞 Referencias

- [Firebase Messaging Docs](https://firebase.flutter.dev/docs/messaging/overview/)
- [Push Notifications Best Practices](https://firebase.google.com/docs/cloud-messaging/concept-options)
- [Android Notification Channels](https://developer.android.com/training/notify-user/channels)
