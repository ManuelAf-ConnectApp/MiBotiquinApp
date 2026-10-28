# Datos, comunicaciones y borrado

Mi Botiquín App funciona sin cuenta remota. La cuenta local, tratamientos, eventos de toma,
inventario, preferencias y copias se guardan en el dispositivo. Las contraseñas y códigos de
recuperación se almacenan como hashes con sal; el identificador de sesión se protege con Android
Keystore o iOS Keychain. Esto no equivale a cifrado de toda la base de datos del dispositivo.

No hay analítica propia, publicidad, correo de recuperación ni sincronización en nube. La app no
debe utilizarse para decisiones médicas ni emergencias.

## Comunicaciones opcionales

- Al consultar CIMA, se envía por HTTPS únicamente el CN indicado a la API pública de AEMPS. No
  se envían perfil, tratamientos, fotos, lote ni serie.
- El lector Android usa Google Play services Code Scanner. La lectura se procesa localmente, pero
  Google puede tratar métricas técnicas conforme a sus condiciones. El lector se instancia solo al
  pulsar el botón; introducir el CN manual no lo inicia.
- iOS usa la cámara mediante AVFoundation, localmente. Las notificaciones son locales del sistema.

## Borrado y copias

Desde Perfil, **Eliminar cuenta** borra la cuenta y los datos relacionados almacenados en su base
local. Borrar un tratamiento elimina además sus eventos e inventario asociados. Las copias que el
usuario haya exportado son archivos independientes: deben eliminarse desde el gestor de archivos
o la ubicación donde se hayan guardado. Desinstalar la app también puede borrar su almacenamiento
local, según la plataforma y sus mecanismos de copia del sistema.

## Límites de los recordatorios

Los avisos dependen de permisos, batería, restricciones del sistema, reinicios, zona horaria y
del sistema de notificaciones elegido por cada plataforma. Android puede usar alarmas aproximadas
si no dispone del permiso de alarmas exactas; iOS mantiene una ventana de hasta 60 avisos y debe
abrirse la app para renovarla. Un aviso mostrado, descartado o pospuesto no confirma una toma.

Este documento describe el comportamiento técnico actual, no sustituye una revisión jurídica para
una publicación pública.
