# PROMPT MAESTRO PARA CLAUDE — MiBotiquin App

> **Rol:** Actúa como arquitecto/a de software senior, desarrollador/a móvil Android/iOS, especialista en UX/UI accesible, persistencia local, notificaciones y mantenimiento de proyectos Open Source. Tu misión es **auditar y evolucionar el repositorio existente**, no crear una aplicación paralela ni reemplazar indiscriminadamente lo ya implementado.
>
> **Idioma de trabajo:** español. Código y nomenclatura técnica según las convenciones reales del proyecto. Documentación principal en inglés y traducción al español cuando se indica.

## 1. Contexto y objetivo

**Nombre público del producto:** MiBotiquin App (mantener la grafía oficial del repositorio cuando corresponda, sin introducir variaciones inconsistentes).

MiBotiquin App es una aplicación **Open Source para teléfonos Android e iOS** destinada a:

1. Registrar usuarios y permitir acceso mediante credenciales almacenadas localmente.
2. Registrar los medicamentos y sus pautas de administración.
3. Programar recordatorios de toma de medicación.
4. Controlar existencias en **comprimidos/pastillas y mililitros**, incluidas dosis fraccionadas de comprimidos.
5. Estimar cuándo se agotará cada medicamento y facilitar su reposición.
6. Escanear un envase o código identificador para recuperar información fiable disponible y precargar el formulario de medicación, siempre con revisión y confirmación del usuario.

El producto es una **herramienta de organización personal**, no un instrumento de diagnóstico ni de prescripción. No recomienda dosis, modificaciones de tratamientos ni sustituciones de medicamentos.

## 2. Instrucciones de ejecución innegociables

- **Primero inspecciona el repositorio**: estructura, tecnología real, arquitectura, librerías, pantallas, recursos, pruebas, scripts y estado de Git. No presupongas Kotlin Multiplatform, Flutter, React Native, Swift ni ninguna tecnología concreta.
- **Conserva** los patrones, dependencias, recursos y componentes válidos. Explica cualquier migración relevante antes de llevarla a cabo.
- **No inventes** que una característica ya funciona: distingue entre *implementado*, *parcial*, *no implementado* y *requiere verificación*.
- Respeta el enfoque **offline-first** de la base de datos local. No presupongas servidor, cuentas en la nube ni sincronización. Toda funcionalidad que exija internet debe ser opcional, identificable y funcionar con una alternativa local razonable cuando sea posible.
- **No introduzcas secretos** (API keys, certificados, contraseñas, datos reales de pacientes) en el código, documentación o repositorio.
- Utiliza las capturas proporcionadas en `screenshot/` como referencias visuales, tras **verificar rutas y contenido reales**. Nunca des por inspeccionada una imagen que no esté disponible.
- Cada cambio debe incluir validación, manejo de errores, estados vacíos, accesibilidad y, cuando resulte aplicable, pruebas.
- Si faltan decisiones de producto, propón alternativas con pros y contras, explicita la opción provisional de menor riesgo y registra las cuestiones bloqueantes; no inventes requisitos clínicos.

## 3. Auditoría inicial obligatoria

Antes de codificar, entrega:

1. **Inventario de pantallas**, navegación, estado de cada funcionalidad y componentes reutilizables.
2. **Mapa de arquitectura**: módulos/capas, modelos, repositorios, almacenamiento, servicios de notificación, permisos y dependencias.
3. **Diagnóstico de UX/UI** frente a las capturas disponibles, incluidos tamaños de pantalla y modo claro/oscuro si ya existen.
4. **Tabla de diferencias** entre estado actual y requisitos de este documento, indicando prioridad, esfuerzo estimado relativo y riesgos.
5. **Plan de implementación incremental**, ordenado en entregables verificables. Evita refactorizaciones masivas innecesarias.

## 4. Navegación y componentes globales

### TopBar

- Componente visual reutilizable, personalizado y consistente con el nuevo sistema de diseño.
- Adaptar título y acciones al contexto; gestionar volver atrás, acciones relevantes y zonas seguras de Android/iOS.
- Evitar duplicidades de títulos o navegación del sistema.

### BottomBar

Una vez iniciada sesión, debe incluir exactamente estas **cuatro secciones principales** (salvo que la arquitectura existente exija una adaptación documentada):

- **Inicio**.
- **Escanear**.
- **Perfil**.
- **Ajustes**.

Destacar la sección activa, asegurar etiquetas comprensibles y soportar accesibilidad y teclado/lectores de pantalla.

### Flujo general

Inicio de sesión / Registro / Recuperación segura → Home → Medicación, stock, escáner, perfil y ajustes. Proteger rutas autenticadas y definir comportamiento de cierre/reinicio de sesión sin perder datos por accidente.

## 5. Autenticación y perfil

### 5.1 Pantalla Login — existente, revisar y mejorar

- Campos **email** y **contraseña**.
- Validación local, feedback de errores sin revelar información sensible, estados de carga cuando proceda y prevención de intentos abusivos.
- Consultar credenciales en la base de datos local **sin almacenar contraseñas en texto claro**; utilizar algoritmo de derivación de contraseñas apropiado con sal y parámetros adecuados a la plataforma.
- Navegar a Home tras autenticación correcta.

### 5.2 Pantalla Registro — existente, revisar y mejorar

- Datos mínimos del perfil: **nombre, apellidos, email, contraseña y confirmación de contraseña** (adaptar a campos reales si ya existen y documentar diferencias).
- Validar obligatoriedad, formato de email, confirmación y duplicados con mensajes claros.
- Establecer y justificar una política de contraseñas segura y usable; no imponer reglas arbitrarias sin motivo.
- Guardar credenciales de forma protegida. Al completar el registro, permitir entrar en Home mediante sesión autenticada.

### 5.3 Pantalla Olvidé contraseña — existente, revisar el diseño de recuperación

**Restricción crítica:** una base de datos exclusivamente local no permite verificar la titularidad de un correo electrónico por el simple hecho de que exista en la tabla de usuarios. **Nunca restablecer una contraseña solo indicando un email conocido.**

- Inspeccionar el procedimiento actual y corregirlo si no verifica identidad.
- Proponer un mecanismo seguro que pueda funcionar sin servidor (por ejemplo, código de recuperación generado al registrarse, con almacenamiento/verificación segura, o un mecanismo equivalente, debidamente especificado).
- Explicar las limitaciones cuando el usuario pierde tanto la contraseña como el mecanismo de recuperación. No prometer recuperación imposible ni hacer pasar por verificación un email no comprobado.
- Proteger datos existentes ante operaciones de restablecimiento.

### 5.4 Pantalla Perfil — existente, revisar

Mostrar **nombre y apellidos, email** del usuario activo. Evaluar la conveniencia de edición de datos, cambio seguro de contraseña, cierre de sesión y opciones de privacidad; implementar únicamente las opciones aprobadas o claramente requeridas, sin añadir datos personales superfluos.

## 6. Home — existente, ampliar

- Dar la bienvenida al usuario autenticado.
- Incluir llamada a la acción **“+ Crear medicación”**.
- Sin medicación registrada: estado vacío instructivo, acción clara de alta.
- Con medicación: lista legible y navegable con nombre, presentación/unidad, próxima toma cuando exista, e indicador comprensible de stock disponible o bajo.
- Permitir abrir detalle y editar medicación, con confirmación adecuada para acciones destructivas.
- Diferenciar *medicamento*, *tratamiento/pauta* y *existencias* para evitar confusiones en cálculos.

## 7. Alta y gestión de medicación — existente, ampliar

### Campos funcionales

- **Medicamento**: nombre identificable, preferentemente seleccionable entre opciones verificadas cuando existan.
- **Dosis por toma**: valor numérico y unidad clara; diferenciar la dosis farmacológica (p. ej., mg) de la cantidad física consumida (comprimidos o mL).
- **Cantidad**: número de pastillas/comprimidos o mililitros según presentación, con precisión adecuada.
- **Fecha de inicio**.
- **Fecha de fin** cuando esté definida.
- **Duración del tratamiento**: cálculo automático, indicando la convención usada (días inclusivos, fechas y tratamiento indefinido).
- **Pauta de administración**: frecuencia, horas concretas y días aplicables, imprescindible para generar recordatorios y estimar consumo. Si todavía no existe, diseñar e implementar su edición con claridad.
- **Stock inicial y stock actual**, con unidades y fecha de referencia, evitando confundir cantidad por toma con cantidad en inventario.

### Fracciones de comprimido

- Permitir elegir explícitamente **¼, ½, ¾ o 1 comprimido** por toma, además de las cantidades enteras cuando corresponda.
- Representar cantidades sin errores acumulativos de coma flotante (p. ej., cuartos como unidades enteras o decimal exacto).
- **No asumir que cualquier medicamento puede fraccionarse**: mostrar aviso contextual y no inferir divisibilidad a partir del formato. La aplicación registra la pauta indicada, no prescribe el fraccionamiento.
- No aplicar fracciones a mililitros como si fueran comprimidos; para líquidos usar un valor decimal con unidad.

### Validaciones y operaciones

- No admitir fechas incompatibles, cantidades negativas, unidades ambiguas ni horarios inválidos.
- Soportar crear, consultar, editar, pausar/finalizar y eliminar tratamientos mediante operaciones seguras.
- La eliminación de medicación asociada a movimientos de stock o tomas requiere decidir entre archivado y borrado definitivo, considerando consistencia y privacidad.
- Cualquier dato precargado por escáner **debe confirmarse antes de guardar**.

## 8. Recordatorios de medicación — requisito central

Diseñar un flujo completo, no solo una interfaz:

1. Configurar horas/días o frecuencia para cada tratamiento.
2. Programar recordatorios locales en Android e iOS con el mecanismo oficial adecuado a la tecnología existente.
3. Solicitar permisos de notificación en el momento apropiado, gestionar denegaciones y explicar límites de cada sistema operativo.
4. Permitir marcar una toma como **tomada**, **omitida** o **pospuesta** si se implementa esta tercera opción; registrar fecha/hora real y prevenir doble contabilización.
5. Reprogramar o cancelar recordatorios cuando se editen, pausen, finalicen o eliminen tratamientos.
6. Considerar reinicio del dispositivo, cambios de zona horaria, horario de verano, cambio manual de fecha, límites de planificación de notificaciones y restricciones de ejecución en segundo plano.
7. No dar por hecho que una notificación mostrada significa que el medicamento se ha tomado.
8. Ofrecer visibilidad sobre próximas tomas y errores de permisos/configuración.

**Regla de inventario:** por defecto, descontar existencias solo cuando exista una toma confirmada o un ajuste manual explícito. Si se propone otra política, documentarla y solicitar aprobación.

### 8.1. Navegación al pulsar una notificación — requisito obligatorio

Cuando el usuario **pulse una notificación de recordatorio de medicación**, la aplicación deberá **abrir directamente una pantalla de detalle de la toma pendiente** (no simplemente la pantalla Home). Esta navegación debe funcionar tanto cuando la aplicación está abierta como cuando se encuentra en segundo plano o cerrada, siempre dentro de las posibilidades de Android e iOS.

**Contenido mínimo de la pantalla de detalle de la toma:**

- Nombre del medicamento y, si existe, presentación identificativa.
- **Cantidad exacta que debe tomar** en esa ocasión, indicando la unidad (p. ej., ¼, ½, ¾ o 1 comprimido; o un número de mL).
- Dosis farmacológica si consta y corresponde, **sin confundirla con el número de comprimidos o volumen**.
- Fecha y hora programadas de la toma.
- Indicaciones o notas guardadas para ese tratamiento, si existen; no generar instrucciones médicas nuevas.
- Estado de esa toma: pendiente, tomada, omitida o pospuesta (si el aplazamiento está implementado).
- Acciones claras para **Confirmar toma** y **Omitir toma**, y **Posponer** solo si esta función está soportada; proporcionar confirmación visual del resultado.

**Comportamiento técnico y casos límite:**

1. Incluir en el identificador/datos de la notificación una referencia estable al **evento de toma concreto** y a su medicamento, sin incorporar información sanitaria sensible innecesaria en el texto visible de la notificación.
2. Al pulsarla, resolver el evento y cargar **los datos actualizados desde la base de datos local**, sin depender de una conexión a Internet ni confiar en datos desactualizados de la notificación.
3. Respetar el inicio de sesión: si la sesión no está activa o el sistema exige desbloqueo, dirigir a autenticación y **continuar después hacia la misma toma**. Impedir que otro usuario acceda a datos del perfil que no le corresponde.
4. Gestionar correctamente las rutas de apertura por notificación, el botón Atrás y la navegación interna en **arranque en frío**, segundo plano y primer plano; evitar pantallas duplicadas y navegación circular.
5. Si la toma ya se confirmó, se eliminó el tratamiento o el recordatorio quedó obsoleto, mostrar un estado comprensible y permitir volver a Inicio; **nunca confirmar ni descontar existencias por el mero hecho de abrir la notificación**.
6. Confirmar la toma de forma idempotente: un doble toque, una reapertura o varias notificaciones no deben provocar dos registros ni dos descuentos de stock.
7. Si llegan varios recordatorios, **cada notificación debe abrir el medicamento y la toma específicos** que le corresponden.
8. Diseñar esta pantalla siguiendo el sistema visual unificado, con texto accesible, botones táctiles claros y soporte para Android e iOS.

**Criterios de aceptación específicos:** pulsar un recordatorio lleva al detalle correcto desde cualquier estado de la app; se muestra medicamento, dosis/cantidad, unidad y horario; la autenticación protege los datos; confirmar la toma actualiza estado y stock exactamente una vez; abrir o descartar una notificación no altera el stock; y se gestionan de forma segura las tomas inexistentes, ya resueltas o pertenecientes a otra cuenta.


## 9. Pantalla Gestión de stock — nueva

- Mostrar existencias por medicamento y presentación en **comprimidos** o **mililitros**, con unidad visible.
- Registrar **stock inicial**, reposiciones (entradas), consumos confirmados (salidas) y ajustes manuales justificados.
- Calcular stock actual a partir de movimientos trazables o de un mecanismo equivalente que preserve integridad y evite duplicados.
- Informar del consumo medio/previsto a partir de las pautas realmente registradas, distinguiendo previsión de consumo confirmado.
- Mostrar **fecha estimada de agotamiento o próxima reposición**, junto con supuestos del cálculo. Si faltan pautas, hay tratamientos variables o datos insuficientes, mostrar “No estimable”, no inventar una fecha.
- Umbrales configurables de stock bajo; indicador visual y, si se aprueba, aviso local.
- Gestionar cambios de tratamiento, reposiciones, dosis fraccionadas y medidas decimales.
- Evitar stock negativo sin aviso y manejar correctamente cambios de unidades/presentaciones.

## 10. Pantalla Escanear — nueva

### Flujo deseado

1. Usuario pulsa **Escanear** en la BottomBar.
2. Solicitar permiso de cámara en contexto, con explicación y alternativa manual.
3. Detectar identificadores disponibles (p. ej., EAN/GTIN o Data Matrix cuando la biblioteca y los códigos del envase lo permitan) y/o aplicar OCR sobre el nombre visible.
4. Resolver la información mediante **fuente de datos fiable y legalmente reutilizable** cuando esté disponible, distinguiendo lo leído directamente del envase de lo obtenido por catálogo.
5. Mostrar pantalla de **confirmación de coincidencia** y advertir si hay resultados ambiguos, no encontrados o incompletos.
6. Abrir el formulario de medicación con **solo los campos verificados precargados**, editables antes de guardar.

### Restricciones

- **Escanear un código no garantiza conocer nombre, dosis, formato o unidades de stock**. No inferir la pauta ni el inventario sin información expresa del usuario.
- Examinar el país/mercado objetivo y disponibilidad de catálogos, licencias, APIs, conectividad y formatos del envase; no elegir una API sin verificación.
- Priorizar reconocimiento local cuando sea viable; si se requiere servicio remoto, pedir consentimiento cuando corresponda y documentar datos enviados, privacidad, costes y modo sin conexión.
- Implementar estados sin permiso, sin cámara, poca luz, error, código desconocido, coincidencias múltiples y modo entrada manual.
- No consultar servicios de terceros con datos personales o historiales de medicación innecesariamente.

## 11. Ajustes — existente, auditar

Analizar si la pantalla actual ofrece los ajustes adecuados. Proponer, de forma priorizada y sin llenar la interfaz de funciones no solicitadas:

- Preferencias de notificaciones y gestión de permisos.
- Umbral de stock bajo (si está implementado).
- Idioma y aspecto visual **solo si la tecnología y el producto lo soportan**.
- Privacidad y gestión de datos locales (exportación/borrado seguros si se aprueban).
- Información legal y de versión; acceso a licencias Open Source.

Mantener coherencia con Perfil y evitar duplicar opciones.

## 12. Rediseño UI/UX a partir de las capturas

**Referencias previstas (comprobar existencia y capitalización de cada ruta):**

| Pantalla | Ruta facilitada | Trabajo esperado |
|---|---|---|
| Login | `screenshot/login.png` | Rediseñar manteniendo función |
| Registro | `screenshot/register.png` | Rediseñar y mejorar validaciones |
| Olvidé contraseña | `screenshot/forgot_password.png` | Rediseñar y adecuar a recuperación segura |
| Perfil | `screnshoot/profile.png` | Verificar si `screnshoot` es un error de ruta; auditar funcionalidad y diseño |
| Ajustes | `Screenshoot/settings.png` | Verificar capitalización y ruta; auditar funcionalidad y diseño |

**Instrucción explícita:** no corregir rutas silenciosamente ni asegurar que se han visto archivos ausentes. Listar las imágenes encontradas y las que falten.

Definir un **sistema de diseño común** y aplicarlo a todas las pantallas, incluidas Home, Medicación, Escáner, Stock y Recordatorios, incluso cuando no exista captura previa. Debe incluir:

- Identidad visual fresca, limpia, confiable y propia de salud digital; paleta, tipografía, espaciado, radios, iconografía y elevaciones consistentes.
- Componentes reutilizables: inputs, selectores de fecha/hora, cards, badges de stock, estados vacíos, confirmaciones y botones.
- Diseño móvil responsive, áreas seguras, tamaños táctiles adecuados, contraste, tipografía adaptable y soporte de tecnologías de asistencia.
- Estados de carga, error, éxito, permisos denegados y conexión ausente.
- Coherencia Android/iOS sin ignorar patrones propios de cada sistema.
- Si hay incertidumbre visual, proponer primero una dirección de diseño y describir cambios pantalla por pantalla.

## 13. Modelo de datos orientativo (adaptar al existente)

Revisar si existen entidades equivalentes y reutilizarlas; considerar conceptualmente:

- `User`: identidad local, perfil y credenciales seguras.
- `Medication`: identificación, forma farmacéutica, unidad, campos descriptivos y origen del escaneo.
- `Treatment`: asociación usuario-medicamento, fechas, estado y pauta.
- `DoseSchedule`: días/horas/frecuencia y cantidad por toma.
- `DoseEvent`: toma prevista, estado y hora efectiva, idempotencia.
- `InventoryLot` o `StockBalance`: existencias por presentación/unidad, si es apropiado.
- `StockMovement`: entradas, consumos y ajustes con referencia al evento para evitar doble descuento.
- `Reminder`: referencia a la programación de notificaciones locales.

Definir claves, índices, unicidad, migraciones, transacciones y política de eliminación. **Aislar los datos por usuario local** y garantizar que un cambio de sesión no expone inventarios o historiales de otra cuenta.

## 14. Seguridad, privacidad y límites sanitarios

- Datos de medicación son **datos de salud sensibles**: aplicar minimización, protección local en reposo cuando sea técnicamente viable, seguridad de claves en mecanismos de plataforma, política de retención y eliminación, protección de copias y diagnósticos sin datos personales.
- No registrar contraseñas, tratamientos ni identificadores personales en logs, analítica o reportes de fallos.
- Valorar bloqueo de sesión y protección frente a acceso casual al dispositivo; documentar amenazas y límites del funcionamiento exclusivamente local.
- Considerar RGPD y legislación aplicable al mercado objetivo, con revisión jurídica antes de publicar afirmaciones de cumplimiento.
- No prometer seguridad absoluta, disponibilidad infalible de recordatorios ni certificaciones médicas no obtenidas.
- Mostrar un aviso proporcionado: la información no sustituye consejo profesional y cualquier cambio de dosis debe seguir indicación sanitaria.

## 15. Documentación y archivos Open Source obligatorios

**El repositorio debe incluir los cinco archivos en su raíz** y sus contenidos deben describir el software real, no funcionalidades futuras como si estuvieran completas:

### `README.md` — inglés, documentación principal

Incluir como mínimo: logo si ya existe y puede usarse legalmente; presentación; características disponibles/en desarrollo; capturas reales; sistemas compatibles y requisitos; stack confirmado; arquitectura y estructura del proyecto; instalación, compilación y ejecución para Android e iOS; permisos de cámara/notificaciones; explicación del almacenamiento local; flujo de escaneo y límites offline/online; recordatorios y limitaciones de plataforma; privacidad y seguridad; pruebas; solución de problemas; contribución; licencia; enlace a README en español y changelog. No inventar versiones, enlaces ni capturas.

### `README.es.md` — español

Traducción fiel y mantenible del README principal, misma cobertura técnica y enlaces cruzados entre ambas versiones.

### `CHANGELOG.md`

Seguir **Keep a Changelog**, con apartado `Unreleased` y versiones/fechas solo si pueden verificarse en Git o en historial existente. Distinguir Added, Changed, Fixed, Deprecated, Removed y Security cuando proceda. No fabricar un historial de lanzamientos.

### `LICENSE`

Agregar el texto **íntegro y oficial de Apache License, Version 2.0 (January 2004)**, sin modificaciones al cuerpo jurídico. Verificar compatibilidad y obligaciones de las dependencias y del código heredado. No asumir titularidad sobre material de terceros.

### `NOTICE`

Incluir avisos y atribuciones legalmente necesarios, incluyendo los NOTICE de terceros que deban conservarse; distinguir esos avisos de agradecimientos voluntarios. No atribuir copyright a personas u organizaciones sin confirmación. Si faltan datos de autoría, dejar marcador `TODO` visible y explicarlo.

**Adicional recomendado:** `.gitignore`, guía de contribución, política de seguridad y listado de dependencias/licencias **si procede**, sin sustituir los cinco archivos obligatorios.

## 16. Calidad, pruebas y criterios de aceptación

Preparar o ampliar pruebas de:

1. Login, registro, duplicados y recuperación segura de contraseña.
2. Aislamiento de datos de dos usuarios y migraciones de base de datos.
3. Alta/edición de medicación, validación de unidades, duración de tratamiento y fracciones de comprimidos.
4. Programación/reprogramación de recordatorios, manejo de permisos y **apertura de la pantalla de detalle de la toma al pulsar la notificación** (app abierta, en segundo plano, cerrada, sesión bloqueada y varios recordatorios).
5. Registro idempotente de toma y descuento único del stock.
6. Reposición, ajuste, stock bajo y previsión de agotamiento, incluidos tratamientos sin pauta conocida.
7. Escaneo exitoso, resultado ambiguo, sin coincidencias y captura manual alternativa.
8. Navegación con TopBar/BottomBar, estados vacíos, accesibilidad y ausencia de regresiones Android/iOS.
9. Presencia, coherencia y enlaces de los cinco documentos Open Source.

**Criterios de aceptación:** proyecto compila para los objetivos que admite realmente el repositorio; pruebas relevantes pasan o se reportan con causa; no se exponen datos sensibles; se preserva información en migraciones; los recordatorios no asumen tomas; las fracciones y mL se calculan correctamente; el escáner no inventa propiedades; documentación coincide con el producto.

## 17. Orden de ejecución propuesto

- **Fase 0 — Auditoría:** inventario técnico/visual, riesgos, decisiones y plan.
- **Fase 1 — Base y autenticación:** corregir seguridad local, navegación, componentes compartidos y diseño base.
- **Fase 2 — Medicación y pautas:** modelo fiable, validaciones, fracciones, persistencia y edición.
- **Fase 3 — Recordatorios y tomas:** notificaciones locales, navegación directa a la toma desde la notificación, pantalla de detalle y eventos de consumo reales.
- **Fase 4 — Stock:** movimientos, cálculo, reposición y alertas.
- **Fase 5 — Escáner:** cámara, lectura, resolución segura, precarga y confirmación.
- **Fase 6 — Rediseño integral, documentación y cierre:** pulido visual, pruebas cruzadas, archivos Open Source y revisión de licencias.

Si una fase depende de una decisión no resuelta, explica el bloqueo y avanza en tareas independientes que no comprometan la arquitectura.

## 18. Formato de respuesta que espero de ti, Claude

### Primera respuesta (antes de cambiar código)

1. Resumen de lo encontrado en el repositorio, con rutas de archivos reales.
2. Qué partes están implementadas, incompletas o ausentes.
3. Evaluación de capturas y propuesta del sistema de diseño.
4. Riesgos críticos (recuperación offline, privacidad, recordatorios, escaneo y consistencia del stock).
5. Plan de trabajo por fases y decisiones que requieran aprobación.

### En cada fase implementada

- Objetivo y alcance.
- Archivos creados/modificados y motivo.
- Decisiones técnicas y dependencias incorporadas.
- Pruebas ejecutadas, resultados y limitaciones que no pudiste verificar.
- Instrucciones para probar manualmente en Android/iOS.
- Actualización pertinente de los README y de `CHANGELOG.md`.

### Resultado final

Entregar proyecto coherente y funcional según el alcance implementado, **sin código ficticio o pantallas simuladas presentadas como completas**, con documentación y licencias verificadas. Incluir lista de pendientes reales y próximos pasos.

---

**Material que acompañará este prompt:** repositorio/código fuente actual de MiBotiquin App y, cuando estén disponibles, las capturas indicadas. Si no has recibido ese material, no afirmes haberlo analizado: indícalo y limita el trabajo inicial a la planificación y especificación.
