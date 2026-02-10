# 📤 Guía de Exportación de Registros - API

## 🧪 PRUEBAS DESDE POSTMAN

### 1. Obtener Token JWT (Primero)

```
POST {{URL}}/api/auth/login
Content-Type: application/json

{
  "identificacion": "tu_identificacion",
  "password": "tu_password"
}
```

Copia el `token` de la respuesta.

---

### 2. Configurar Headers en Postman

Para TODAS las peticiones de exportación:

```
Authorization: Bearer <tu_token_jwt>
Content-Type: application/json
```

---

## 📊 ENDPOINTS DE EXPORTACIÓN

### Para EMPLEADO (USER)

#### Ver meses disponibles para exportar

```
GET {{URL}}/api/registros/exportar/meses
```

**Respuesta ejemplo:**

```json
[
  {
    "mes": 2,
    "anio": 2026,
    "nombreMes": "FEBRERO",
    "cantidadRegistros": 15,
    "proximoAEliminar": false
  }
]
```

#### Ver info de limpieza automática

```
GET {{URL}}/api/registros/limpieza/info
```

**Respuesta ejemplo:**

```json
{
  "hayAdvertencia": false,
  "mensaje": "No hay registros programados para eliminación automática.",
  "mesAEliminar": null,
  "anioAEliminar": null,
  "diasRestantes": null,
  "fechaEliminacion": null,
  "cantidadRegistros": 0,
  "puedeExportar": false
}
```

#### Exportar MIS registros a PDF

```
POST {{URL}}/api/registros/exportar/pdf
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

#### Exportar MIS registros a Excel

```
POST {{URL}}/api/registros/exportar/excel
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

#### Exportar MIS registros a Word

```
POST {{URL}}/api/registros/exportar/word
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

---

### Para ADMIN

#### Ver meses disponibles (todos los empleados)

```
GET {{URL}}/api/admin/exportar/meses
```

#### Exportar TODOS los registros a PDF

```
POST {{URL}}/api/admin/exportar/pdf
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

#### Exportar TODOS los registros a Excel

```
POST {{URL}}/api/admin/exportar/excel
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

#### Exportar TODOS los registros a Word

```
POST {{URL}}/api/admin/exportar/word
Content-Type: application/json

{
  "mes": 2,
  "anio": 2026
}
```

---

## 📱 CÓMO DESCARGAR EN POSTMAN (PRUEBA)

### Paso a paso:

1. Haz la petición POST de exportación
2. En Postman, ve a la pestaña **"Body"** de la respuesta
3. Cambia de "Pretty" a **"Preview"** (para PDF) o haz clic en **"Save Response"** → **"Save to a file"**
4. Guarda el archivo con la extensión correcta:
   - `.pdf` para PDF
   - `.xlsx` para Excel
   - `.docx` para Word
5. Abre el archivo descargado

### Configuración importante en Postman:

- En **Settings** → desactiva "Automatically follow redirects" si hay problemas
- El archivo se descarga como **binary**, no como JSON

---

## 📱 IMPLEMENTACIÓN EN FLUTTER (Para tu hermano)

### Dependencias necesarias (pubspec.yaml):

```yaml
dependencies:
  dio: ^5.4.0
  path_provider: ^2.1.2
  open_filex: ^4.4.0
  permission_handler: ^11.3.0
```

### Código Flutter para descargar:

```dart
import 'dart:io';
import 'package:dio/dio.dart';
import 'package:path_provider/path_provider.dart';
import 'package:open_filex/open_filex.dart';
import 'package:permission_handler/permission_handler.dart';

class ExportService {
  final Dio _dio;
  final String baseUrl;

  ExportService(this._dio, this.baseUrl);

  /// Exportar registros a PDF/Excel/Word
  /// [formato] puede ser: 'pdf', 'excel', 'word'
  /// [esAdmin] true si es ADMIN, false si es USER
  Future<void> exportarRegistros({
    required int mes,
    required int anio,
    required String formato,
    required String token,
    bool esAdmin = false,
  }) async {
    try {
      // Pedir permisos de almacenamiento
      if (Platform.isAndroid) {
        await Permission.storage.request();
      }

      // Determinar endpoint según rol
      String endpoint = esAdmin
          ? '/api/admin/exportar/$formato'
          : '/api/registros/exportar/$formato';

      // Obtener directorio de descargas
      Directory? directory;
      if (Platform.isAndroid) {
        directory = Directory('/storage/emulated/0/Download');
      } else {
        directory = await getApplicationDocumentsDirectory();
      }

      // Nombre del archivo
      String extension = formato == 'excel' ? 'xlsx' : (formato == 'word' ? 'docx' : 'pdf');
      String fileName = 'Registros_${_getNombreMes(mes)}_$anio.$extension';
      String filePath = '${directory.path}/$fileName';

      // Hacer la petición
      Response response = await _dio.post(
        '$baseUrl$endpoint',
        data: {
          'mes': mes,
          'anio': anio,
        },
        options: Options(
          headers: {
            'Authorization': 'Bearer $token',
            'Content-Type': 'application/json',
          },
          responseType: ResponseType.bytes,
        ),
      );

      // Guardar archivo
      File file = File(filePath);
      await file.writeAsBytes(response.data);

      // Abrir archivo
      await OpenFilex.open(filePath);

      print('✅ Archivo guardado en: $filePath');
    } catch (e) {
      print('❌ Error al exportar: $e');
      rethrow;
    }
  }

  /// Obtener meses disponibles para exportar
  Future<List<Map<String, dynamic>>> obtenerMesesDisponibles({
    required String token,
    bool esAdmin = false,
  }) async {
    String endpoint = esAdmin
        ? '/api/admin/exportar/meses'
        : '/api/registros/exportar/meses';

    Response response = await _dio.get(
      '$baseUrl$endpoint',
      options: Options(
        headers: {'Authorization': 'Bearer $token'},
      ),
    );

    return List<Map<String, dynamic>>.from(response.data);
  }

  /// Obtener información de limpieza automática
  Future<Map<String, dynamic>> obtenerInfoLimpieza({
    required String token,
    bool esAdmin = false,
  }) async {
    String endpoint = esAdmin
        ? '/api/admin/limpieza/info'
        : '/api/registros/limpieza/info';

    Response response = await _dio.get(
      '$baseUrl$endpoint',
      options: Options(
        headers: {'Authorization': 'Bearer $token'},
      ),
    );

    return Map<String, dynamic>.from(response.data);
  }

  String _getNombreMes(int mes) {
    const meses = [
      '', 'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
      'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
    ];
    return meses[mes];
  }
}
```

### Uso en Flutter:

```dart
// En tu widget o controlador
final exportService = ExportService(Dio(), 'https://tu-api.com');

// Exportar a PDF
await exportService.exportarRegistros(
  mes: 2,
  anio: 2026,
  formato: 'pdf',
  token: tuTokenJWT,
  esAdmin: false, // o true si es admin
);

// Obtener meses disponibles
final meses = await exportService.obtenerMesesDisponibles(
  token: tuTokenJWT,
);

// Mostrar advertencia de limpieza
final infoLimpieza = await exportService.obtenerInfoLimpieza(
  token: tuTokenJWT,
);

if (infoLimpieza['hayAdvertencia'] == true) {
  // Mostrar alerta al usuario
  showDialog(
    context: context,
    builder: (ctx) => AlertDialog(
      title: Text('⚠️ Advertencia'),
      content: Text(infoLimpieza['mensaje']),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(ctx),
          child: Text('Entendido'),
        ),
        ElevatedButton(
          onPressed: () {
            Navigator.pop(ctx);
            // Ir a pantalla de exportación
          },
          child: Text('Exportar ahora'),
        ),
      ],
    ),
  );
}
```

---

## 🎨 UI SUGERIDA PARA FLUTTER

### Pantalla de Exportación:

```dart
class ExportScreen extends StatefulWidget {
  @override
  _ExportScreenState createState() => _ExportScreenState();
}

class _ExportScreenState extends State<ExportScreen> {
  List<Map<String, dynamic>> mesesDisponibles = [];
  int? mesSeleccionado;
  int? anioSeleccionado;
  bool cargando = false;

  @override
  void initState() {
    super.initState();
    _cargarMeses();
  }

  Future<void> _cargarMeses() async {
    // Cargar meses disponibles
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Exportar Registros')),
      body: Column(
        children: [
          // Selector de mes
          DropdownButton<int>(
            hint: Text('Seleccionar mes'),
            value: mesSeleccionado,
            items: mesesDisponibles.map((m) {
              return DropdownMenuItem<int>(
                value: m['mes'],
                child: Row(
                  children: [
                    Text('${m['nombreMes']} ${m['anio']}'),
                    if (m['proximoAEliminar'] == true)
                      Icon(Icons.warning, color: Colors.orange),
                  ],
                ),
              );
            }).toList(),
            onChanged: (value) {
              setState(() {
                mesSeleccionado = value;
                anioSeleccionado = mesesDisponibles
                    .firstWhere((m) => m['mes'] == value)['anio'];
              });
            },
          ),

          SizedBox(height: 20),

          // Botones de formato
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _botonExportar('PDF', Icons.picture_as_pdf, Colors.red),
              _botonExportar('Excel', Icons.table_chart, Colors.green),
              _botonExportar('Word', Icons.description, Colors.blue),
            ],
          ),
        ],
      ),
    );
  }

  Widget _botonExportar(String formato, IconData icono, Color color) {
    return ElevatedButton.icon(
      style: ElevatedButton.styleFrom(backgroundColor: color),
      icon: Icon(icono, color: Colors.white),
      label: Text(formato, style: TextStyle(color: Colors.white)),
      onPressed: mesSeleccionado == null ? null : () => _exportar(formato.toLowerCase()),
    );
  }

  Future<void> _exportar(String formato) async {
    setState(() => cargando = true);
    try {
      await exportService.exportarRegistros(
        mes: mesSeleccionado!,
        anio: anioSeleccionado!,
        formato: formato,
        token: tuToken,
      );
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('✅ Archivo descargado correctamente')),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('❌ Error: $e')),
      );
    }
    setState(() => cargando = false);
  }
}
```

---

## ⚠️ MOSTRAR ADVERTENCIA DE LIMPIEZA

En la pantalla principal o al iniciar la app:

```dart
Future<void> verificarAdvertenciaLimpieza() async {
  final info = await exportService.obtenerInfoLimpieza(token: token);

  if (info['hayAdvertencia'] == true) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Row(
          children: [
            Icon(Icons.warning, color: Colors.orange),
            SizedBox(width: 8),
            Text('Advertencia'),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(info['mensaje']),
            SizedBox(height: 10),
            Text(
              'Registros a eliminar: ${info['cantidadRegistros']}',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text('Recordar después'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.blue),
            onPressed: () {
              Navigator.pop(ctx);
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => ExportScreen()),
              );
            },
            child: Text('Exportar ahora'),
          ),
        ],
      ),
    );
  }
}
```

---

## 📋 RESUMEN DE RESPUESTAS

| Endpoint                  | Respuesta                                                                                 |
| ------------------------- | ----------------------------------------------------------------------------------------- |
| `GET .../exportar/meses`  | JSON array con meses disponibles                                                          |
| `GET .../limpieza/info`   | JSON con info de limpieza                                                                 |
| `POST .../exportar/pdf`   | Archivo binario (application/pdf)                                                         |
| `POST .../exportar/excel` | Archivo binario (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)       |
| `POST .../exportar/word`  | Archivo binario (application/vnd.openxmlformats-officedocument.wordprocessingml.document) |
