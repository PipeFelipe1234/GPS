# 📤 Exportación y Limpieza Automática de Registros

## Descripción General

Este módulo permite:

1. **Exportar registros** en formatos PDF, Excel (XLSX) y Word (DOCX)
2. **Limpieza automática** de registros antiguos (después de 2 meses)
3. **Notificaciones de advertencia** 4 días hábiles antes de la eliminación

---

## 🕐 Regla de Limpieza Automática

- **Los registros se eliminan automáticamente después de 2 meses**
- Ejemplo: Si estamos en **Abril**, se eliminan los registros de **Febrero**
- Los registros de **Marzo** (mes anterior) siguen visibles
- La limpieza se ejecuta el **primer día de cada mes a las 00:05 AM**

### Advertencias

- **4 días hábiles antes** de la eliminación, los usuarios reciben una notificación:
  > "⚠️ Se eliminarán automáticamente los registros del Mes: [NOMBRE_MES] en [X] días, por favor exporte los registros de ese Mes"

---

## 📱 Endpoints para Usuarios (USER)

### Obtener información de limpieza

```http
GET /api/registros/limpieza/info
```

**Response:**

```json
{
  "hayAdvertencia": true,
  "mensaje": "⚠️ Se eliminarán automáticamente los registros del Mes: FEBRERO 2026 en 4 día(s) hábil(es). Por favor exporte los registros de ese mes.",
  "mesAEliminar": "FEBRERO",
  "anioAEliminar": 2026,
  "diasRestantes": 4,
  "fechaEliminacion": "2026-04-01",
  "cantidadRegistros": 45,
  "puedeExportar": true
}
```

### Obtener meses disponibles para exportar

```http
GET /api/registros/exportar/meses
```

**Response:**

```json
[
  {
    "mes": 2,
    "anio": 2026,
    "nombreMes": "FEBRERO",
    "cantidadRegistros": 45,
    "proximoAEliminar": true
  },
  {
    "mes": 3,
    "anio": 2026,
    "nombreMes": "MARZO",
    "cantidadRegistros": 30,
    "proximoAEliminar": false
  }
]
```

### Exportar a PDF

```http
POST /api/registros/exportar/pdf
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

**Response:** Archivo PDF descargable

### Exportar a Excel

```http
POST /api/registros/exportar/excel
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

**Response:** Archivo XLSX descargable

### Exportar a Word

```http
POST /api/registros/exportar/word
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

**Response:** Archivo DOCX descargable

---

## 👮 Endpoints para Administradores (ADMIN)

### Obtener información de limpieza

```http
GET /api/admin/limpieza/info
```

### Obtener meses disponibles

```http
GET /api/admin/exportar/meses
```

### Exportar TODOS los registros a PDF

```http
POST /api/admin/exportar/pdf
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

### Exportar TODOS los registros a Excel

```http
POST /api/admin/exportar/excel
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

### Exportar TODOS los registros a Word

```http
POST /api/admin/exportar/word
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

### Forzar eliminación de un mes (SOLO ADMIN)

```http
DELETE /api/admin/registros/limpiar/{mes}/{anio}
```

**Ejemplo:**

```http
DELETE /api/admin/registros/limpiar/2/2026
```

**Response:**

```json
{
  "mensaje": "Se eliminaron 45 registros del mes de FEBRERO 2026",
  "eliminados": 45,
  "mes": "FEBRERO",
  "anio": 2026
}
```

---

## 📊 Diferencias entre USER y ADMIN

| Característica               | USER | ADMIN |
| ---------------------------- | ---- | ----- |
| Ver sus propios registros    | ✅   | ✅    |
| Ver todos los registros      | ❌   | ✅    |
| Exportar sus registros       | ✅   | ✅    |
| Exportar todos los registros | ❌   | ✅    |
| Forzar eliminación           | ❌   | ✅    |
| Recibir advertencias         | ✅   | ✅    |

### Contenido de las exportaciones

**Para USER:**

- Fecha
- Hora Entrada
- Hora Salida
- Horas Trabajadas
- Minutos Trabajados
- Ubicación Entrada
- Ubicación Salida
- Reporte
- Estado (En curso / Finalizado)
- Imagen (URL)

**Para ADMIN (incluye datos del empleado):**

- Fecha
- Empleado (nombre)
- Identificación
- Cargo
- Teléfono
- Hora Entrada
- Hora Salida
- Horas Trabajadas
- Ubicación Entrada
- Ubicación Salida
- Reporte
- Imagen (URL)

---

## 🔧 Configuración Técnica

### Tareas Programadas (Cron)

| Tarea                 | Horario       | Descripción                |
| --------------------- | ------------- | -------------------------- |
| Limpieza automática   | `0 5 0 * * *` | Todos los días a las 00:05 |
| Envío de advertencias | `0 0 9 * * *` | Todos los días a las 09:00 |

### Dependencias Añadidas

```xml
<!-- Apache POI para Excel y Word -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>

<!-- OpenPDF para PDF -->
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>1.3.30</version>
</dependency>
```

---

## 📁 Archivos Creados/Modificados

### Nuevos archivos:

- `dto/ExportRequest.java` - Request para exportación
- `dto/CleanupInfoResponse.java` - Response con info de limpieza
- `service/ExportService.java` - Servicio de exportación
- `service/ScheduledCleanupService.java` - Servicio de limpieza programada

### Archivos modificados:

- `pom.xml` - Nuevas dependencias
- `BackendApplication.java` - Habilitado @EnableScheduling
- `RegistroRepository.java` - Nuevos queries
- `TokenDispositivoRepository.java` - Nuevo método
- `NotificacionService.java` - Nuevo método
- `RegistroController.java` - Endpoints de exportación USER
- `AdminController.java` - Endpoints de exportación ADMIN

---

## 🚀 Flujo de Uso Recomendado

### Para Flutter:

1. **Al iniciar la app**, consultar `/api/registros/limpieza/info`
2. Si `hayAdvertencia == true`, mostrar un **banner o diálogo** con el mensaje
3. Ofrecer botón para **exportar** antes de que se eliminen
4. Mostrar lista de meses disponibles con `/api/registros/exportar/meses`
5. Al seleccionar un mes, ofrecer opciones: PDF, Excel, Word
6. Llamar al endpoint correspondiente y descargar el archivo

### Ejemplo en Flutter:

```dart
// Verificar advertencia al iniciar
final response = await http.get(
  Uri.parse('$baseUrl/api/registros/limpieza/info'),
  headers: {'Authorization': 'Bearer $token'},
);

final info = CleanupInfoResponse.fromJson(jsonDecode(response.body));

if (info.hayAdvertencia) {
  showDialog(
    context: context,
    builder: (context) => AlertDialog(
      title: Text('⚠️ Advertencia'),
      content: Text(info.mensaje),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: Text('Entendido'),
        ),
        ElevatedButton(
          onPressed: () => _exportarRegistros(info.mesAEliminar, info.anioAEliminar),
          child: Text('Exportar Ahora'),
        ),
      ],
    ),
  );
}
```

---

## ✅ Notas Importantes

1. **Las imágenes en los documentos** se muestran como URLs. Para incrustar las imágenes directamente en el PDF/Word, se requeriría procesamiento adicional.

2. **Días hábiles**: La cuenta regresiva considera solo días de lunes a viernes.

3. **Zona horaria**: Las tareas programadas usan la zona horaria del servidor.

4. **Respaldo**: Siempre exporte los registros antes de que sean eliminados automáticamente.
