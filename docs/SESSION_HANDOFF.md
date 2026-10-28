# Punto de reanudación — 25 de octubre de 2026

## Instrucciones vigentes

- Cierre de sesión: el usuario pide guardar la información y continuar mañana.
- Proyecto open source, sin despliegues ni publicación en Google Play o Apple App Store.
- Lista vigente acordada: [TASKS.md](TASKS.md). Sustituye los pendientes orientados a tiendas.
- Al retomar, consultar esa lista; no se ha elegido todavía una tarea concreta para mañana.

- El usuario eligió convertir el acceso simulado en autenticación local funcional.
- Después pidió terminar las pruebas **sin generar APK**. No ejecutar assemble/package sin una nueva petición.
- No se ha solicitado publicación, despliegue, commit ni push.

## Estado integrado

La fuente de autenticación usa Room local: registro e inicio de sesión comprueban contraseñas
PBKDF2 y actualizan contraseñas antiguas en claro al primer acceso correcto. No hay correo ni
sincronización en la nube.
La sesión usa un identificador local estable y almacenamiento de plataforma; el repositorio
no elimina medicación al cerrar sesión. Recordarme controla la restauración del acceso.
La recuperación por código está registrada en Koin y expuesta desde Perfil y la pantalla de
restablecimiento. Generar un código exige la contraseña actual; se muestra una sola vez y se
consume al cambiar la contraseña.

Integrados desde la copia de cierre, adaptados al código actual:
- Cantidades exactas, pauta básica, duración inclusiva, pausa y datos opcionales.
- Eventos persistidos, detalle, confirmar/omitir y apertura desde avisos nativos.
- Stock con recuento explícito, movimientos, previsión y consumo transaccional único.
- Escáner España, CN manual y consulta CIMA opcional con confirmación.
- Diseño compartido, previews y versión instalada desde plataforma.
- Migraciones explícitas 5→11, sin borrado destructivo; esquema v11 generado mediante KSP.
- Esquemas v6/v7 recuperados del commit histórico c27c31b, sin fabricar hashes.

## Validación terminada

Comando ejecutado con éxito (sin tareas de APK):

```sh
./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest :data:iosSimulatorArm64Test :domain:iosSimulatorArm64Test
python3 scripts/verify_recovery_sql.py
```

- JVM Android host: data 32 + domain 37, sin fallos (69).
- Simulador iOS: data registra 38 + domain 36, sin fallos. Un caso de data retorna
  anticipadamente cuando Keychain no está disponible: 73 comprobaciones ejercitadas,
  Keychain pendiente en app firmada. No interpretar ese retorno como validación de Keychain.
- SQLite auxiliar: 11 comprobaciones, sin fallos.
- RecoveryMigrationTest usa src/sqliteTest compartida por iosTest/androidDeviceTest:
  el runner JVM de Android no puede cargar la biblioteca JNI Android en macOS.
- KSP Android/iOS genera el esquema Room v11. La compilación Android de presentation y
  composeApp pasó en el intento previo; el empaquetado completo no se ha completado.
- APK existente en outputs: fechado el 23 de octubre; no se ha generado uno nuevo.

## Fase de validación adicional terminada

- RoomMigrationTest abre archivos SQLite temporales con el builder de producción y el
  validador KSP real: v5/v6/v7/v8→v11 pasan, conservando cuenta, medicación y cantidades
  antiguas sin inventar stock. Las escrituras nuevas persisten al reabrir el archivo.
- v4 rechaza la apertura sin borrar su cuenta, medicación ni versión.
- Las fixtures se copian de los esquemas exportados v4–v8; comprobar su correspondencia:
  `python3 scripts/generate_migration_fixtures.py --check`.
- Seis regresiones nuevas de sincronización de avisos cubren sesión ausente/cambiada,
  desactivación durante planificación y errores temporales.
- Corregido SyncRemindersUseCase: vuelve a leer la preferencia tras planificar para no
  programar avisos que el usuario acaba de desactivar, incluso cuando falla la planificación.
- Detalles y límites: docs/migration-validation.md.

## Pendientes

La lista completa y ordenada está en [TASKS.md](TASKS.md): documentación/contribución,
autenticación, conservación de datos, dispositivos, accesibilidad, funciones,
automatización, licencias y privacidad. No incluye publicación ni requisitos de tiendas.
La firma de desarrollo iOS solo se contempla para ejecución y validación local.

No ejecutar una build para generar APK mientras siga vigente la restricción del usuario.
Los cambios siguen en el árbol de trabajo, sin commit ni push.
