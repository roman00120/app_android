# Total Ground Catálogo Mobile (Android Nativo)

Aplicación móvil nativa en **Kotlin** y **Jetpack Compose** para consultar el catálogo de productos, fichas técnicas en PDF y visualizar modelos 3D interactivos en formato GLB con el motor gráfico **SceneView / Google Filament**.

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)
![3D Engine](https://img.shields.io/badge/3D%20Engine-SceneView%20%2F%20Filament-orange.svg)
![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)

---

## 📱 Características Principales

- ⚡ **Catálogo 100% Offline:** Consulta rápida y fluida de más de 350 productos sin dependencia de conexión a internet.
- 🎯 **Filtrado por Categorías y Búsqueda en Tiempo Real:** Búsqueda instantánea con normalización de texto y filtro por categorías/subcategorías estables.
- 🧊 **Visor 3D GLB Nativo:** Renderizado 3D PBR acelerado por hardware con gestos táctiles (rotación de cámara con un dedo, zoom con pinza, botones de recentrado y restablecimiento de vista).
- 📄 **Fichas Técnicas PDF:** Apertura e inspección directa de documentos técnicos asociados a cada producto.
- 🎨 **Diseño Moderno Total Ground:** Cumple con las guías oficiales de diseño e identidad visual de la marca, incluyendo icono adaptativo oficial.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Kotlin
- **UI Framework:** Jetpack Compose & Material 3
- **Arquitectura:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Motor 3D:** SceneView 2.2.1 (Google Filament Engine)
- **Carga de Imágenes:** Coil
- **Concurrencia:** Kotlin Coroutines & StateFlow
- **Inyección / Deserialización:** Gson

---

## 📦 Descargas (Releases)

Puedes descargar el archivo instalable **APK** de la versión más reciente en la sección de **[Releases](https://github.com/roman00120/app_android/releases)**:

- 🚀 **[Descargar APK Total Ground Catálogo v1.0.4](https://github.com/roman00120/app_android/releases/download/v1.0.4/TotalGround-Catalogo-v1.0.4.apk)**

---

## ⚙️ Estructura del Proyecto

```text
app/src/main/
├── assets/
│   ├── models/            # Archivos 3D .GLB reales de los productos
│   ├── data/              # Base de datos JSON local (productos, categorías)
│   └── documents/         # Fichas técnicas en formato PDF
└── java/com/totalground/app/
    ├── data/
    │   ├── model/         # Data Models (ApiProduct, ApiCategory, etc.)
    │   └── repository/    # CatalogRepository local con gestión de caché
    └── ui/
        ├── components/    # Componentes reutilizables de Jetpack Compose
        ├── navigation/    # Grafo de navegación de la app
        ├── screens/       # Pantallas (CatalogScreen, ProductDetailScreen, GlbViewerScreen)
        └── theme/         # Sistema de diseño, colores y tipografía de la marca
```
