# Contribuir a Mi Botiquín App

Gracias por contribuir. Este es un proyecto open source que se ejecuta localmente en Android
y iOS; no tiene despliegue ni publicación en Google Play o App Store.

## Preparación

Consulta los requisitos y las instrucciones de ejecución en el [README](README.md). Usa el
wrapper incluido: `./gradlew`. No incluyas `local.properties`, datos de salud, tokens, bases de
datos personales ni capturas con información real en una contribución.

La autenticación es local a la instalación. No la presentes como una cuenta remota ni como
un sistema de recuperación por correo. El código de recuperación requiere la contraseña actual
para generarse y debe tratarse como secreto.

## Flujo de trabajo

1. Abre una incidencia antes de emprender un cambio grande o que modifique los datos.
2. Trabaja en una rama con un propósito concreto.
3. Mantén los cambios pequeños y conserva los datos existentes.
4. Añade o ajusta las pruebas relevantes para reglas de dominio, persistencia o migraciones.
5. Actualiza documentación y traducciones cuando cambie el comportamiento visible.
6. Envía una solicitud de cambios que explique el comportamiento anterior, el nuevo y la
   validación realizada.

No introduzcas generación de APK, publicación, despliegues o credenciales de tienda como parte
del flujo de contribución.

## Comprobaciones locales

Ejecuta las que correspondan al cambio:

```sh
./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest
./gradlew :data:iosSimulatorArm64Test :domain:iosSimulatorArm64Test
python3 scripts/verify_recovery_sql.py
python3 scripts/generate_migration_fixtures.py --check
git diff --check
```

Estas pruebas no sustituyen la aceptación en dispositivos. Las migraciones 5–8 a 11 se prueban
con Room en el simulador iOS. Las versiones 1–4 aún no están soportadas: no propongas borrar
datos como solución.

## Datos, privacidad y avisos

La aplicación registra información relacionada con medicación. Usa datos ficticios en pruebas,
incidencias, comentarios y capturas. Una notificación no confirma una toma. La consulta opcional
a CIMA envía el código nacional que el usuario ha confirmado, y el lector Android depende de
Google Play services. Consulta [NOTICE](NOTICE), [NOTICE.md](NOTICE.md) y la documentación de
escáner antes de modificar esas partes.

## Estilo y revisiones

Mantén la arquitectura por módulos (`domain`, `data`, `presentation`, `composeApp`) y no
introduzcas dependencias de plataforma en `domain`. Evita textos de interfaz codificados: añade
recursos equivalentes en español e inglés. Los esquemas Room deben ser generados por KSP; nunca
inventes hashes de esquema.

Las contribuciones se revisan por corrección funcional, conservación de datos, cobertura
proporcional, accesibilidad y claridad de la documentación.
