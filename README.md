# Mi Botiquin App - Kotlin Multiplatform

**Mi Botiquin App** es una aplicación desarrollada con **Kotlin Multiplatform (KMP)** diseñada para ayudar a los usuarios a gestionar sus medicamentos y recordatorios. Está construida siguiendo los principios de **Clean Architecture** y el patrón **MVI (Model-View-Intent)**, orientada a **Android** e **iOS** utilizando **Compose Multiplatform**.

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
- [x] **Acceso local**: Registro e inicio de sesión locales con contraseñas PBKDF2, sesión protegida y recuperación mediante un código de un solo uso generado desde Perfil.
- [x] **Panel de Control**: Acceso rápido a los horarios diarios de medicación.
- [x] **Perfil y Ajustes**: Gestionar preferencias de usuario y seguridad de la cuenta.
- [x] **UI Moderna**: Sistema de diseño Material 3 con soporte para modo oscuro.

## Estado de integración y pruebas

Se han integrado cantidades exactas, tomas persistidas, stock, escáner y recordatorios sobre
la autenticación local actual. Las sesiones conservan un identificador local estable y
cerrar sesión no elimina la medicación. El código de recuperación se genera desde Perfil y
se consume al restablecer la contraseña; no se envían correos ni hay sincronización en la nube.
Room v11 se ha generado con KSP; las migraciones desde v1 hasta v8 pasan con el
validador real de Room en iOS. [Validación de migraciones](docs/migration-validation.md).

Validación: 69 pruebas JVM y 77 comprobaciones iOS ejercitadas pasan, además de 11 comprobaciones SQLite.
La suite iOS registra otro caso de Keychain que retorna sin ejercitar el almacén cuando el
servicio no está disponible; su aceptación requiere una app firmada. Las cámaras, CIMA real,
notificaciones y accesibilidad siguen pendientes de prueba en dispositivo.
No se ha generado un APK nuevo; la última instrucción del usuario limita la ejecución a pruebas.

```sh
./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest
./gradlew :data:iosSimulatorArm64Test :domain:iosSimulatorArm64Test
python3 scripts/verify_recovery_sql.py
```

## 🏁 Primeros Pasos

### Requisitos previos

El repositorio usa el wrapper de Gradle 9.6.0, Kotlin 2.4.0, Android Gradle Plugin 9.4.1
y Compose Multiplatform 1.11.1. No instales Gradle globalmente.

* JDK 17 o posterior compatible con Gradle/AGP.
* Android Studio con Android SDK Platform 37 y el SDK configurado en `local.properties`.
  Android requiere API 26 o posterior para ejecutar la aplicación.
* macOS con Xcode para iOS. Configura un equipo de desarrollo en Xcode si quieres probar
  Keychain o avisos en un dispositivo; no se publica en App Store.
* Acceso a internet la primera vez que Gradle descargue sus dependencias. El escáner de
  Android puede descargar su módulo de Google Play services al utilizarse por primera vez.

Consulta [CONTRIBUTING.md](CONTRIBUTING.md) para las pruebas, convenciones y límites del
proyecto open source.

### Construir y Ejecutar

#### Android

Abre el proyecto en Android Studio, permite que se complete la sincronización y ejecuta la
configuración `composeApp` desde el IDE. Para comprobar la lógica sin empaquetar una aplicación:

```sh
./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest
python3 scripts/verify_recovery_sql.py
```

#### iOS

Abre `iosApp/iosApp.xcodeproj` con Xcode, elige un simulador o dispositivo y ejecuta el
esquema de la aplicación. Las pruebas compartidas se pueden ejecutar sin crear un paquete:

```sh
./gradlew :data:iosSimulatorArm64Test :domain:iosSimulatorArm64Test
```

Las cuentas son locales a cada instalación. La contraseña se comprueba localmente y puedes
generar un código de recuperación de un solo uso desde Perfil. No se envían correos ni hay
sincronización en la nube. Consulta [docs/TASKS.md](docs/TASKS.md) para las funciones pendientes
y [SECURITY.md](SECURITY.md) para informar vulnerabilidades.

---

*Desarrollado con ❤️ para una mejor gestión de la salud.*
