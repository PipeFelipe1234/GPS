# 🔍 Filtros de Búsqueda para Registros de Asistencia - ADMIN

## Endpoint

**POST** `/api/admin/registros/filtrar`

## Descripción

Permite al ADMIN buscar registros de asistencia aplicando filtros opcionales por:

- **Fecha**: Filtra registros por una fecha específica (formato: YYYY-MM-DD)
- **Identificación**: Busca registros del usuario por su identificación (búsqueda parcial, case-insensitive)
- **Nombres**: Busca registros del usuario por su nombre (búsqueda parcial, case-insensitive)

## Parámetros de Entrada (JSON)

```json
{
  "fecha": "2026-02-06", // Opcional: YYYY-MM-DD
  "identificacion": "123456", // Opcional: string (búsqueda parcial)
  "nombres": "Juan" // Opcional: string (búsqueda parcial)
}
```

## Ejemplos de Uso

### 1️⃣ Buscar por Fecha exacta

```json
{
  "fecha": "2026-02-06",
  "identificacion": null,
  "nombres": null
}
```

Retorna todos los registros del 6 de febrero de 2026.

### 2️⃣ Buscar por Identificación

```json
{
  "fecha": null,
  "identificacion": "123456",
  "nombres": null
}
```

Retorna todos los registros del usuario con identificación que contenga "123456".

### 3️⃣ Buscar por Nombres

```json
{
  "fecha": null,
  "identificacion": null,
  "nombres": "Juan"
}
```

Retorna todos los registros de usuarios cuyo nombre contenga "Juan".

### 4️⃣ Buscar Combinado (Fecha + Identificación)

```json
{
  "fecha": "2026-02-06",
  "identificacion": "123456",
  "nombres": null
}
```

Retorna registros del 6 de febrero de 2026 del usuario con identificación que contenga "123456".

### 5️⃣ Búsqueda Completa (Todos los Filtros)

```json
{
  "fecha": "2026-02-06",
  "identificacion": "123",
  "nombres": "Juan"
}
```

Retorna registros que cumplan TODAS las condiciones:

- Fecha: 6 de febrero de 2026
- Identificación contiene: "123"
- Nombres contienen: "Juan"

### 6️⃣ Sin Filtros (Obtener Todos)

```json
{
  "fecha": null,
  "identificacion": null,
  "nombres": null
}
```

Retorna todos los registros de asistencia (equivalente a GET `/api/admin/registros`).

## Respuesta

Array de registros que cumplen los criterios:

```json
[
  {
    "id": 1,
    "fecha": "2026-02-06",
    "horaEntrada": "08:30:00",
    "horaSalida": "17:30:00",
    "latitud": 4.711,
    "longitud": -74.0721,
    "precisionMetros": 10.5,
    "latitudCheckin": 4.711,
    "longitudCheckin": -74.0721,
    "precisionMetrosCheckin": 8.3,
    "reporte": "Día productivo",
    "picture": "url_foto.jpg",
    "identificacionUsuario": "123456",
    "nombreUsuario": "Juan Pérez",
    "fotoUsuario": "url_perfil.jpg",
    "telefonoUsuario": "3101234567"
  }
]
```

## Características

✅ Búsqueda case-insensitive (mayúsculas y minúsculas)
✅ Búsqueda parcial en identificación y nombres
✅ Filtros opcionales (puedes combinarlos)
✅ Búsqueda exacta en fechas
✅ Retorna todos los campos relacionados del usuario y el registro

## Nota Técnica

- Utiliza **JPA Query** para búsquedas eficientes
- Los valores `null` en los filtros son ignorados
- La búsqueda en texto es case-insensitive y parcial (LIKE)
- Retorna DTO `RegistroResponse` con información completa
