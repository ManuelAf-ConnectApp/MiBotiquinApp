# Punto de reanudación

El usuario ha pedido guardar todo y continuar el lunes. No hay despliegue ni publicación solicitados.

## Estado guardado

- Recuperación local mediante código de un solo uso, sesiones e aislamiento de medicación.
- Cantidades exactas, fracciones, pautas básicas, pausa y duración inclusiva.
- Eventos persistidos, detalle, confirmar/omitir y apertura desde notificaciones Android/iOS.
- Rediseño basado en las cuatro referencias de screenshots/, con previews.
- Stock con inicialización explícita, movimientos, ajustes justificados, umbrales, previsiones
  y descuento único en la misma transacción que confirma la toma.
- Escáner para España: cámara nativa, CN manual, códigos EAN/NTIN compatibles y consulta
  CIMA opcional con confirmación. No se infiere dosis ni stock del envase.
- README principal en inglés, README.es.md, CHANGELOG.md, LICENSE y NOTICE presentes.
- La versión instalada se consulta desde cada plataforma en Ajustes.

## Evidencia disponible

- 46 comprobaciones Kotlin/JUnit y 11 comprobaciones SQLite pasan.
- El lector iOS y el cliente HTTP compilan en aislamiento con Kotlin/Native 2.4.0.
- Recursos/traducciones, manifiestos, catálogo de versiones, enlaces de README y
  git diff --check verificados.
- El usuario confirmó una build anterior de autenticación, no la build actual completa.

## Primeros pasos al retomar

1. Compilar el estado actual desde una terminal normal:

   ```sh
   ./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest :composeApp:assembleDebug
   ./gradlew :data:iosSimulatorArm64Test :domain:iosSimulatorArm64Test
   ```

2. Resolver cualquier error real de Compose/KSP/dependencias nativas y generar/revisar
   el esquema Room v11. El esquema v8 existente fue generado; no fabricar hashes/esquemas.
3. Probar stock/tomas y navegación por notificaciones en dispositivos y validar la nueva
   dependencia Google Code Scanner 16.1.0. Probar cámara iOS y consulta CIMA real:
   no se ha confirmado una consulta de catálogo en vivo.
4. Revisar el renderizado del nuevo diseño, texto ampliado, teclado, ambos temas e idiomas.
5. Revisar los TODO de avisos transitivos, atribución heredada y privacidad antes de publicar.

El entorno restringido del agente impide arrancar los bloqueos/sockets normales de Gradle.
Las comprobaciones auxiliares no sustituyen la build completa:

```sh
python3 scripts/run_domain_checks.py
python3 scripts/verify_recovery_sql.py
python3 scripts/run_native_adapter_checks.py
```

## Documentación para continuar

- docs/stock-validation.md
- docs/scanner-spain.md
- docs/reminder-validation.md
- docs/design-system.md

Pendientes funcionales adicionales: horarios arbitrarios por días/horas, posposición,
historial completo/exportación/archivado, diagnóstico de permisos y migraciones 1–4.
Estas versiones antiguas fallan sin borrar sus datos; no recomendar desinstalar para migrar.

## Guardado local

El commit de cierre no pudo crearse: el entorno deniega escribir `.git/index.lock`.
Los archivos permanecen guardados en el árbol de trabajo. Hay una copia adicional en
`.checkpoints/session-checkpoint.tar.gz`, con cambios, archivos nuevos y referencia al commit base.
La carpeta de copias está excluida de Git. No se ha hecho push.
