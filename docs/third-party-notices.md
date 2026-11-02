# Dependencias y atribuciones

El código del proyecto está bajo Apache-2.0: [LICENSE](../LICENSE). Las dependencias mantienen sus
licencias propias. La lista de primer nivel declarada por el proyecto está en
[NOTICE.md](../NOTICE.md), y el aviso distribuido está en [NOTICE](../NOTICE).

Además de Kotlin, kotlinx, Compose/AndroidX y Koin, se declaran Cryptography Kotlin (Apache-2.0),
SQLite bundled (Apache-2.0) y Google Play services Code Scanner (condiciones de Google, no
relicenciado por este repositorio). AEMPS/CIMA aporta información de catálogo bajo sus propias
condiciones; no se redistribuye un catálogo ni se implica respaldo de AEMPS.

Antes de distribuir binarios, ejecuta la inspección de dependencias de Gradle incluida en CI y
conserva los avisos de artefactos transitivos que exijan atribución. El logo `MiBotiquinApp.png`
y los iconos derivados son recursos propios generados con IA bajo la dirección del titular que
figura en NOTICE; no pertenecen a las dependencias de terceros.

En Android, el plugin `com.google.android.gms.oss-licenses-plugin` recoge los términos declarados
en los POM del classpath y los empaqueta junto con `play-services-oss-licenses`; la actividad de
avisos queda declarada en el manifiesto. Antes de publicar iOS, conserva por separado los avisos
del framework Kotlin/Native y de cualquier artefacto nativo incluido en el paquete.
