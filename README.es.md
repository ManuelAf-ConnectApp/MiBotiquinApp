# Mi Botiquín App

<img src="commonResources/src/commonMain/composeResources/drawable/app_logo.png" width="96" alt="Logo de Mi Botiquín App">

[English](README.md) · [Changelog](CHANGELOG.md) · [Licencia](LICENSE) · [Avisos](NOTICE)

Organizador personal de medicación Android/iOS con Kotlin Multiplatform y Compose. Las
cuentas, tratamientos, tomas e inventario se guardan localmente. La identificación opcional
de medicamentos españoles consulta CIMA/AEMPS por acción expresa del usuario. La app registra
la pauta del usuario; no diagnostica, prescribe ni recomienda fraccionar comprimidos.

## Implementación actual y validación

| Área | Implementado en código |
|---|---|
| Cuentas locales | Registro/login, hashes PBKDF2 con sal, sesión protegida en plataforma, Recordarme, cierre y borrado de cuenta |
| Recuperación | Código de un solo uso generado desde Perfil tras verificar contraseña; restablecimiento local con correo, código y nueva contraseña |
| Medicación | Cantidades exactas en cuartos de comprimido y mL, fechas, duración inclusiva, tratamiento indefinido/en pausa, dosis farmacológica y notas opcionales |
| Tomas | Eventos pendientes, detalle, confirmar/omitir, hora de registro, aislamiento e idempotencia |
| Notificaciones | Programación nativa Android/iOS y apertura de la toma concreta, continuando tras autenticación |
| Stock | Recuento inicial explícito, reposiciones, ajustes justificados, umbral de stock bajo, historial y previsión de primera toma no cubierta |
| Escáner España | Google Code Scanner en Android / AVFoundation en iOS, EAN/NTIN españoles compatibles, CN manual, consulta CIMA opcional y confirmación |
| Interfaz | Diseño claro/oscuro basado en capturas y formularios compartidos accesibles |

**La validación es parcial:** pasan 46 pruebas Kotlin/JUnit y 11 comprobaciones SQLite. El
lector iOS y el cliente HTTP también compilan por separado con Kotlin/Native. El usuario
confirmó una compilación anterior de autenticación. La compilación completa actual
Gradle/Compose/KSP, la nueva dependencia Android, las cámaras/CIMA reales y la matriz nativa
siguen pendientes. Implementado en código no equivale a probado en dispositivo.

## Plataformas, tecnología y estructura

Android requiere SDK mínimo 26; compilación/destino están en [el catálogo de versiones](gradle/libs.versions.toml).
Usa Android Studio y un JDK compatible con las versiones Gradle/AGP del proyecto. iOS necesita
macOS/Xcode y los ajustes de despliegue/firma de `iosApp/iosApp.xcodeproj`.

- `domain`: modelos, validaciones, pautas/previsiones y contratos de repositorios.
- `data`: Room/SQLite, repositorios, almacenamiento protegido y notificaciones/HTTP nativos.
- `presentation`: Compose, ViewModels, navegación, acceso a recursos y cámara nativa.
- `composeApp`: entradas Android/iOS y módulos Koin.
- `commonResources`: traducciones, dimensiones, logo y recursos compartidos.

Se conserva la arquitectura de estado/intenciones/efectos de estilo MVI, Navigation3,
coroutines, kotlinx.datetime, serialization y Koin. El escáner Android añade
`com.google.android.gms:play-services-code-scanner:16.1.0`, sujeto a condiciones propias de Google.

## Compilar y ejecutar

Abre el checkout en Android Studio, deja resolver las dependencias y ejecuta `composeApp`.

```sh
./gradlew :composeApp:assembleDebug
./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest
```

Para iOS abre `iosApp/iosApp.xcodeproj`, elige esquema/dispositivo y configura la firma local.
Pruebas de simulador:

```sh
./gradlew :data:iosSimulatorArm64Test :domain:iosSimulatorArm64Test
```

La compilación completa debe generar y revisar el esquema actual Room **v11**. Solo deben
incorporarse esquemas producidos por KSP; no se inventan hashes de identidad.

## Datos locales, recuperación y privacidad

No hay cuenta en la nube ni sincronización de tratamientos. Cada cuenta aísla sus operaciones
de medicación, tomas y stock. Contraseñas nuevas y códigos usan hashes PBKDF2 con sal; las
contraseñas antiguas en claro se actualizan tras un login correcto. La sesión usa almacenamiento
respaldado por Android Keystore o iOS Keychain. No se afirma que toda la base sanitaria esté
cifrada. La configuración Android existente desactiva las copias de seguridad.

Tras registrarte, abre **Perfil → Código de recuperación**, verifica tu contraseña y guarda
el código fuera del dispositivo en un lugar privado. Generar otro sustituye el anterior. Un
restablecimiento correcto lo consume y cierra sesión sin borrar medicación. El correo es un
identificador local; no verifica la titularidad del buzón. Sin contraseña ni código previamente
generado no se puede recuperar el acceso. Desinstalar o borrar datos puede perder la información.

Desde Perfil puedes borrar explícitamente cuenta y datos asociados. Borrar una medicación
elimina también su historial de tomas/stock, con confirmación. No incluyas información sanitaria
real o secretos en logs, incidencias ni capturas. La pantalla de privacidad explica el producto;
no es una política de cumplimiento revisada jurídicamente.

## Reglas de stock

Configura el recuento inicial con lo que tienes físicamente **ahora**. Se registra su fecha de
referencia; no se reconstruyen consumos anteriores. El stock sin configurar es desconocido,
no cero. Las reposiciones y ajustes crean movimientos trazables; los ajustes exigen motivo.
Los identificadores de operación evitan repetir una operación al reintentar.

Con stock configurado, la primera confirmación, el saldo y el movimiento único de consumo se
guardan en una transacción. Reabrir, confirmar dos veces, omitir o descartar un aviso no vuelve
a descontar. Si faltan existencias, la toma queda pendiente con un error: revisa/repón o corrige
el recuento explícitamente. Sin stock configurado se puede registrar la toma sin descuento.
No se permiten saldos negativos ni desbordamientos.

Se utilizan enteros en milésimas; los comprimidos requieren múltiplos de ¼. Con inventario,
la unidad/identidad del producto no puede cambiar silenciosamente. Crea otra medicación para
un producto/presentación diferente. La previsión indica la primera toma futura que el saldo
no cubre, hasta 365 días, sin reposiciones ni omisiones. Excluye tomas atrasadas pendientes y
nunca descuenta por estimación. Sin pauta suficiente o con pausa no inventa fechas.
Consulta [la validación de stock](docs/stock-validation.md).

## Identificación española y consulta opcional

**Escanear** lee identificadores EAN-13/NTIN Data Matrix compatibles o admite un CN manual de
seis dígitos. Los GTIN desconocidos y variantes GS1 no soportadas requieren CN manual: no se
deducen medicamento, pauta, existencias ni divisibilidad. No hay QR/OCR ni resolución GTIN internacional general.

Al pulsar **Consultar CIMA** se envía una petición HTTPS pública con el CN solicitado; el
cliente de catálogo no envía perfil, historial, fotos, lote ni serie. Si falla o no hay internet,
siempre puedes crear la medicación manualmente. Confirma la presentación exacta antes de abrir
el formulario. Solo se precargan nombre y referencia de catálogo; debes elegir cantidad, unidad
y frecuencia. La concentración del envase no se interpreta como dosis prescrita. La procedencia
y fecha se conservan mientras se conserva el nombre verificado; renombrarlo manualmente elimina
esa atribución al guardar.

Android delega la lectura en Google Play services sin permiso CAMERA propio; puede necesitar
la descarga inicial del módulo. El SDK se crea solo al pulsar la lectura. Google puede recibir
métricas técnicas de uso/rendimiento: [condiciones y privacidad](https://developers.google.com/ml-kit/terms).
La entrada manual evita iniciar ese lector. iOS solicita cámara en contexto y usa AVFoundation;
sin cámara o permiso queda disponible el CN manual. Ninguna vía envía imágenes a CIMA.

Fuente: [documentación CIMA REST](https://cima.aemps.es/cima/resources/docs/CIMA_REST_API.pdf) e
[indicaciones de identificadores AEMPS](https://www.aemps.gob.es/informa/la-aemps-actualiza-la-informacion-a-incluir-en-la-aplicacion-de-notificacion-de-comercializacion-de-medicamentos-y-sobre-dispositivos-de-seguridad/).
Se muestran atribución y fecha de consulta; no se implica respaldo ni redistribución de un
catálogo completo. [Implementación y pruebas del escáner](docs/scanner-spain.md).

## Notificaciones y permisos

Android pide POST_NOTIFICATIONS cuando corresponde. Se comprueba acceso a alarmas exactas;
sin él se usa un mecanismo aproximado. Reinicios y cambios de hora/zona recalculan eventos.
Android renueva la ventana al recibir alarmas; iOS planifica como máximo 60 avisos pendientes
y necesita volver a abrir la app para renovarlos. Cierre forzado, permisos y restricciones del
sistema pueden impedir o retrasar avisos. Mostrar una notificación nunca confirma una toma.
El contenido visible es genérico y los datos contienen una referencia al evento.

Pulsarla abre el detalle correspondiente y continúa tras autenticación si hace falta. Otra
cuenta no puede leerlo ni resolverlo. Se distinguen pautas pausadas/cambiadas, eventos borrados
y tomas ya resueltas. [Matriz de aceptación nativa](docs/reminder-validation.md).

## Referencias de diseño y verificaciones

Estas imágenes facilitadas son **referencias de diseño**, no capturas de una nueva build validada:
[Login](screenshots/login.png), [Registro](screenshots/register.png),
[Recuperación](screenshots/forgot_password.png), [Ajustes](screenshots/settings.png).

[Sistema de diseño](docs/design-system.md) · Previews de Android Studio:
`presentation/src/commonMain/kotlin/com/connectapp/presentation/preview/DesignPreviews.kt`.

```sh
python3 scripts/run_domain_checks.py
python3 scripts/verify_recovery_sql.py
```

Las comprobaciones con compilador en caché/SQLite no sustituyen Gradle, KSP, Compose ni pruebas
en dispositivo. El entorno restringido del agente no puede iniciar los bloqueos/sockets normales
de Gradle; ejecuta la build desde una terminal normal. Las nuevas dependencias Google pueden
necesitar red. Las bases 1–4 fallan al abrir **sin borrar datos**; no desinstales como solución.
Se incluyen migraciones 5→11 comprobadas con SQLite; quedan validación Room y exportación actual.

## Pendientes y contribución

Pendientes: aceptación/build nativa e inventario de licencias; horarios arbitrarios por días;
posposición, historial completo/exportación, archivado, diagnóstico de notificaciones, migraciones
1–4, OCR/GTIN general y política de privacidad revisada para publicación. No se presupone nube.
Mantén cambios incrementales, conserva datos, añade pruebas de dominio/persistencia relevantes
y actualiza ambos README y [CHANGELOG.md](CHANGELOG.md).

El código usa [Apache License 2.0](LICENSE). Conserva [NOTICE](NOTICE) y [NOTICE.md](NOTICE.md)
heredado. SDK Google, frameworks de plataforma e información AEMPS tienen condiciones propias.
La autoría/atribución del logo y el inventario completo de avisos transitivos requieren confirmación
antes de publicar; véanse los TODO explícitos de NOTICE.
