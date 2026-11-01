# Tareas pendientes

Lista acordada con el usuario al cerrar la sesión del 25 de octubre de 2026.
La primera sección se completó el 26 de octubre de 2026.

## Alcance e instrucciones

- Proyecto open source para que cualquier persona pueda compilarlo y ejecutarlo.
- No hay despliegues ni publicación en Google Play ni Apple App Store.
- Quedan fuera fichas de tiendas, revisión de tiendas y flujos de despliegue/publicación.
- El acceso local funcional se eligió e integró el 26 de octubre de 2026.
- No generar APK ni ejecutar tareas assemble/package sin una nueva petición del usuario.
- No se ha solicitado commit ni push.

## 1. Documentación y contribución

- [x] Documentar requisitos y pasos reproducibles para Android/iOS.
- [x] Añadir guía de contribución, plantillas de incidencias y política de seguridad.
- [x] Distinguir funciones disponibles, simuladas y pendientes.

## 2. Autenticación

- [x] Documentar el acceso local y sus límites sin nube ni correo.
- [x] Decidir con el usuario e implementar acceso local funcional.
- [x] Conectar la recuperación por código de un solo uso a Perfil y al restablecimiento.

## 3. Conservación de datos

- [x] Validar migraciones en Android y con copias de bases antiguas (protocolo preparado; pruebas manuales por parte de ManuAF).
- [x] Resolver el soporte de versiones 1–4 sin borrar datos.
- [x] Incorporar exportación y copia/restauración local (núcleo cifrado y selector Android listos; pruebas manuales por parte de ManuAF, incluido selector/aceptación iOS).

## 4. Validación en dispositivos

- [x] Tomas y descuento único de stock (pruebas automáticas y matriz preparadas; pruebas manuales por parte de ManuAF).
- [x] Notificaciones, permisos, reinicios y cambios de hora/zona (matriz preparada; pruebas manuales por parte de ManuAF).
- [x] Cámaras y consultas CIMA reales (validación y matriz preparadas; pruebas manuales por parte de ManuAF).
- [x] Keychain en una ejecución iOS con firma de desarrollo (pruebas manuales por parte de ManuAF; no requiere publicación en tienda).

## 5. Interfaz y accesibilidad

- [x] Revisar temas, idiomas, texto ampliado, teclado y pantallas pequeñas (revisión estática y matriz preparadas; pruebas manuales por parte de ManuAF).
- [x] Comprobar lectores de pantalla (semántica revisada y matriz preparada; pruebas manuales por parte de ManuAF).

## 6. Funciones pendientes

- [x] Horarios personalizados diarios (lista de horas `HH:mm`).
- [x] Horarios personalizados por día de la semana.
- [x] Posposición persistente de recordatorios (10 minutos).
- [x] Historial de tomas y archivado no destructivo de tratamientos.
- [x] Diagnóstico de notificaciones (estado del sistema, alarmas exactas y avisos locales programados).
- [ ] OCR de texto (el reconocimiento general de GTIN ya valida GTIN-8/12/13/14 y GS1 AI (01)).

## 7. Calidad y automatización

- [x] Configurar integración continua para pruebas y comprobaciones del repositorio.
- [x] Automatizar verificación de traducciones, esquemas Room y fixtures de migración.
- [x] Revisar compatibilidad y avisos de dependencias (CI deja visibles los avisos de Gradle).

## 8. Licencias y privacidad

- [x] Completar inventario operativo de licencias y atribuciones de dependencias, logo y recursos.
- [x] Documentar almacenamiento local y comunicaciones con CIMA/Google.
- [x] Explicar borrado de datos y limitaciones de los recordatorios.

## Trabajo ya validado

No volver a tratar como pendientes las pruebas terminadas:
- 69 pruebas JVM, 77 comprobaciones iOS ejercitadas y 11 comprobaciones SQLite sin fallos.
- Migraciones v1--v8→v11 verificadas con Room real en el simulador iOS.
- Corregida la carrera al desactivar recordatorios durante la planificación.
- Un caso adicional de Keychain retorna sin comprobar el servicio si no está disponible:
  su validación sigue pendiente en una app con firma de desarrollo.

Estado técnico y comandos: [SESSION_HANDOFF.md](SESSION_HANDOFF.md).
Evidencia de migraciones: [migration-validation.md](migration-validation.md).
