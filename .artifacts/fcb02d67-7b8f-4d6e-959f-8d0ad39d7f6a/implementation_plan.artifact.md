# Eliminación de Licencia Apache 2.0 y Referencias a Software Libre

El usuario desea comercializar la aplicación y, por lo tanto, quiere eliminar la licencia Apache 2.0 y cualquier mención al proyecto como "Software Libre" o "Open Source".

## Proposed Changes

### [Component Root]

Eliminación de los archivos de licencia y actualización de la documentación principal.

#### [DELETE] [LICENSE](file:///Users/manuelalconchel/AndroidStudioProjects/ControlPharmaApp/LICENSE)
Eliminación del archivo de licencia Apache 2.0 completo.

#### [MODIFY] [NOTICE.md](file:///Users/manuelalconchel/AndroidStudioProjects/ControlPharmaApp/NOTICE.md)
Eliminación de la mención de que la aplicación se distribuye bajo Apache 2.0. Se mantendrán las atribuciones a terceros (librerías) ya que es una buena práctica (y a menudo requisito legal) incluso en software comercial.

#### [MODIFY] [README.md](file:///Users/manuelalconchel/AndroidStudioProjects/ControlPharmaApp/README.md)
Eliminación de la sección "Licencia" que referencia a Apache 2.0.

### [Component :composeApp]

#### [MODIFY] [build.gradle.kts](file:///Users/manuelalconchel/AndroidStudioProjects/ControlPharmaApp/composeApp/build.gradle.kts)
Opcional: Se podría eliminar el bloque `packaging.resources.excludes` que menciona `AL2.0` y `LGPL2.1`, aunque este bloque suele estar para evitar conflictos de archivos en el APK final y no implica que la app sea open source. Lo mantendré por ahora para evitar errores de compilación por duplicados en el APK, a menos que el usuario prefiera eliminarlo explícitamente.

## Verification Plan

### Automated Tests
- No se requieren cambios en los tests.

### Manual Verification
- Verificar que los archivos `LICENSE` han desaparecido.
- Revisar `README.md` y `NOTICE.md` para asegurar que no hay menciones a "Apache 2.0" o "Software Libre" aplicadas a la app.
