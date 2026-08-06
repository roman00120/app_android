# Total Ground Catálogo Mobile

Aplicación móvil nativa Android para consultar el catálogo de productos, fichas técnicas y modelos 3D GLB de Total Ground.

## Descargas

Descarga la versión más reciente de la aplicación directamente desde la sección de **[Releases](https://github.com/roman00120/app_android/releases)**.

- **Última versión**: [TotalGround-Catalogo-v1.0.4.apk](https://github.com/roman00120/app_android/releases/download/v1.0.4/TotalGround-Catalogo-v1.0.4.apk)

## Novedades en v1.0.4

- **Integración Completa de 50 Modelos 3D GLB Reales:** Todos los 47 productos con modelos 3D en `productos.json` cargan su archivo física y binariamente verificado (`models/ID.glb`) de forma estricta 1 a 1.
- **Asociación Explícita por `productos.json`:** Eliminados fallbacks, búsquedas parciales y selecciones difusas. La visibilidad del botón "Ver Modelo 3D" depende exclusivamente de la existencia del archivo GLB.
