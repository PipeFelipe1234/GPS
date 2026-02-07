# ⚡ Guía Rápida de Configuración Firebase

## 1️⃣ Descargar Credenciales Firebase (2 minutos)

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto → **Project Settings** (⚙️)
3. Pestaña **Service Accounts**
4. Botón **Generate New Private Key**
5. Se descarga un archivo JSON: `practica-backend-firebase-adminsdk-xxxxx.json`

## 2️⃣ Guardar en el Proyecto (1 minuto)

1. Abre la carpeta raíz del backend:

   ```
   C:\Users\ANDRES FELIPE\Documents\backend\
   ```

2. Copia el archivo descargado aquí

3. Renómbralo a: `firebase-key.json`

4. Abre `.gitignore` y agrega:
   ```
   firebase-key.json
   ```

## 3️⃣ Listo, ¡sin cambios en el código!

Cuando hagas `mvn spring-boot:run`, el backend:

- ✅ Buscará automáticamente `firebase-key.json`
- ✅ Inicializará Firebase
- ✅ Estará listo para enviar notificaciones

---

## 🧪 Verificar que Funciona

1. **Inicia el backend:**

   ```bash
   cd C:\Users\ANDRES FELIPE\Documents\backend
   .\mvnw.cmd spring-boot:run
   ```

2. **Busca este mensaje en los logs:**

   ```
   ✅ Firebase inicializado correctamente
   ```

3. **Si ves ese mensaje, ¡estás listo!** 🎉

---

## 🔗 URLs Importantes

| Servicio             | URL                                         |
| -------------------- | ------------------------------------------- |
| Backend              | `http://localhost:8080`                     |
| Firebase Console     | https://console.firebase.google.com         |
| API Registrar Token  | `POST /api/notificaciones/registrar-token`  |
| API Desactivar Token | `POST /api/notificaciones/desactivar-token` |

---

## 📱 Para tu Hermano (Frontend Flutter)

Cuéntale que:

1. ✅ El backend ya envía notificaciones automáticamente
2. ✅ Solo necesita implementar:
   - Obtener Token FCM
   - Registrarlo en backend (POST `/api/notificaciones/registrar-token`)
   - Escuchar notificaciones con `FirebaseMessaging.onMessage`
3. ✅ Ver código en archivo: `FLUTTER_FCM_GUIDE.md`

---

## ⚠️ Troubleshooting

| Problema                              | Solución                                             |
| ------------------------------------- | ---------------------------------------------------- |
| "⚠️ No se encontró firebase-key.json" | Verifica que el archivo esté en la raíz del proyecto |
| "❌ Firebase no está inicializado"    | Revisa los logs de inicio                            |
| "No se envían notificaciones"         | Verifica que existan ADMINs con tokens registrados   |
| "Error 401 al registrar token"        | JWT debe ser válido y usuario debe estar logeado     |

---

## 📞 Próximos Pasos

1. **Hoy:** Descargar `firebase-key.json` y guardarlo ✅
2. **Mañana:** Tu hermano comienza con Firebase en Flutter
3. **Prueba:** Marcar entrada/salida y recibir notificación en tiempo real 🚀

---

**¡Listo para notificaciones push!** 📲
