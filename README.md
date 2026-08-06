# Total Ground Catálogo Mobile

Aplicación móvil nativa Android para consultar el catálogo de productos, fichas técnicas y modelos 3D GLB de Total Ground.

## Descargas

Descarga la versión más reciente de la aplicación directamente desde la sección de **[Releases](https://github.com/roman00120/app_android/releases)**.

- **Última versión**: [TotalGround-Catalogo-v1.0.4.apk](https://github.com/roman00120/app_android/releases/download/v1.0.4/TotalGround-Catalogo-v1.0.4.apk)

## Novedades en v1.0.4

- **Asociación Estricta 1 a 1 de Modelos 3D:** Eliminada toda asignación ambigua o difusa por coincidencia parcial. Cada producto abre única y exclusivamente su modelo `.glb` real asignado de forma explícita (`glbFile`).
- **Ocultamiento de Botón 3D en Productos sin GLB Confirmado:** Los 342 productos que no cuentan con un archivo 3D real no muestran el botón "Ver Modelo 3D", evitando cruce o sustitución de modelos entre productos.
- **Limpieza de Estado al Navegar:** Se garantiza que al salir y entrar a otro producto, el visor limpia el motor 3D y la memoria sin reutilizar URLs o modelos previos.
