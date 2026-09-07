# Pharma Control App - Kotlin Multiplatform

**Pharma Control App** es una aplicación desarrollada con **Kotlin Multiplatform (KMP)** diseñada para ayudar a los usuarios a gestionar sus medicamentos y recordatorios. Está construida siguiendo los principios de **Clean Architecture** y el patrón **MVI (Model-View-Intent)**, orientada a **Android** e **iOS** utilizando **Compose Multiplatform**.

## 🚀 Descripción del Proyecto

El objetivo principal de esta aplicación es proporcionar una experiencia unificada para el seguimiento de la medicación, incluyendo horarios de dosis, recordatorios y gestión de inventario. Aprovecha las últimas tecnologías de Kotlin para compartir la lógica de negocio entre plataformas manteniendo una interfaz de usuario de alta calidad y nativa.

## 🏗️ Arquitectura

El proyecto sigue los principios de Clean Architecture distribuidos en varios módulos:

### 1. `:domain` (Lógica de Negocio Principal)
*   **Pure Kotlin**: Sin dependencias específicas de plataforma.
*   **Modelos**: Entidades de dominio como `User` y `Medication`.
*   **Repositorios**: Definición de interfaces para operaciones de datos.
*   **Casos de Uso**: Implementación de reglas de negocio (ej. `SaveMedicationUseCase`, `LoginUseCase`).

### 2. `:data` (Infraestructura)
*   **Implementación de Repositorios**: Implementaciones concretas de las interfaces de dominio.
*   **Fuentes de Datos**: Lógica para llamadas a API remotas y persistencia local.
*   **Mappers**: Conversión de datos entre DTOs y modelos de Dominio.

### 3. `:presentation` (Lógica de UI y Gestión de Estado)
*   **Patrón MVI**: Gestión de estado predecible mediante `State`, `Intent` y `Effect`.
*   **ViewModels**: Poseedores de estado que interactúan con la capa de dominio.
*   **Pantallas**: UI compartida con Composable para ambas plataformas.
*   **Componentes**: Elementos de UI reutilizables adaptados al contexto de Pharma.

### 4. `:composeApp` (Puntos de Entrada de Plataforma)
*   **Common Main**: Configuración compartida de DI (Koin) y flujo principal de la aplicación.
*   **Android Main**: Actividad y configuración de Android.
*   **iOS Main**: Punto de entrada de iOS y configuración del ViewController.

### 5. `:commonResources` (Recursos Compartidos)
*   **Recursos**: Cadenas de texto, imágenes y fuentes compartidas utilizadas en todos los módulos.
*   **Código Generado**: Acceso a recursos tanto desde Android como desde iOS.
*   **`TokenResources.kt`**: Punto único de acceso a todas las cadenas de texto (`Res.string.*`) usadas por la capa `presentation`; ningún texto de UI debe hardcodearse en los Composables.
*   **`DimensResources.kt`**: Escala centralizada de valores `Dp` (`Dimen2`…`Dimen120`) usada por la capa `presentation`; ningún `.dp` debe hardcodearse en los Composables.

## 🛠️ Stack Tecnológico

*   **Lenguaje**: [Kotlin](https://kotlinlang.org/)
*   **UI Framework**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
*   **Inyección de Dependencias**: [Koin](https://insert-koin.io/)
*   **Persistencia**: [Room (KMP)](https://developer.android.com/kotlin/multiplatform/room)
*   **Navegación**: [Navigation3](https://developer.android.com/jetpack/compose/navigation)
*   **Concurrencia**: Kotlin Coroutines & Flow
*   **Gestión de Estado**: MVI (Model-View-Intent)
*   **Fecha y Hora**: Kotlinx Datetime
*   **Recursos**: Compose Resources (`:commonResources`) para cadenas de texto y dimensiones (`Dp`) centralizadas
*   **Notificaciones**: `AlarmManager` (Android) / `UNUserNotificationCenter` (iOS) tras la abstracción `NotificationManager` del dominio

## ✨ Características Principales

- [x] **Gestión de Medicamentos**: Añadir y seguir dosis, cantidades y duraciones.
- [x] **Recordatorios**: Notificaciones locales configurables (hora de inicio, dosis/día, aviso previo) implementadas de forma nativa en cada plataforma — `AlarmManager` en Android y `UNUserNotificationCenter` en iOS — a través de la abstracción `domain.manager.NotificationManager`.
- [x] **Autenticación**: Flujos seguros de Login, Registro y Recuperación de Contraseña.
- [x] **Panel de Control**: Acceso rápido a los horarios diarios de medicación.
- [x] **Perfil y Ajustes**: Gestionar preferencias de usuario y seguridad de la cuenta.
- [x] **UI Moderna**: Sistema de diseño Material 3 con soporte para modo oscuro.

## 🏁 Primeros Pasos

### Requisitos Previos
*   Android Studio Ladybug o posterior.
*   Xcode (para desarrollo iOS).
*   Plugin de Kotlin Multiplatform.

### Construir y Ejecutar

#### Android
- Desde Android Studio: Selecciona la configuración de ejecución `composeApp`.
- Desde la Terminal:
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```

#### iOS
- Abre el directorio `iosApp` en Xcode y ejecuta el proyecto.
- O utiliza la configuración de ejecución `iosApp` en Android Studio.

---

*Desarrollado con ❤️ para una mejor gestión de la salud.*
