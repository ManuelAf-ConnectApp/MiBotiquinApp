# Política de seguridad

## Versiones y alcance

La rama principal es la única versión mantenida. Mi Botiquín App es software open source de
ejecución local; no ofrece un servicio de cuentas ni sincronización. Las cuentas, contraseñas
y códigos de recuperación permanecen en el dispositivo.

No utilices la aplicación para decisiones médicas, diagnósticos ni situaciones de emergencia.
Los avisos pueden retrasarse o no entregarse por permisos, restricciones del sistema, cierres
forzados o cambios de fecha y zona horaria.

## Informar una vulnerabilidad

No publiques detalles explotables, datos personales, información sanitaria, contraseñas, códigos
de recuperación, tokens, bases de datos ni capturas con datos reales en una incidencia pública.

Abre una incidencia con el título `Security: contacto privado solicitado`, sin describir el
problema sensible. El mantenedor responderá en la propia incidencia con un canal privado para
recibir los detalles. Incluye entonces: versión o commit, plataforma, pasos mínimos con datos
ficticios, impacto y una posible mitigación si la conoces.

Si el hallazgo no permite esperar al intercambio privado, limita la incidencia pública a una
descripción general y una forma de contacto segura. No adjuntes pruebas de concepto que accedan
a datos de terceros.

## Qué ayuda a investigar

Son especialmente útiles los informes sobre aislamiento entre cuentas locales, pérdida de datos
en migraciones, exposición de información en notificaciones, almacenamiento de secretos,
validación de CIMA o comportamiento inseguro del escáner. Indica si el comportamiento ocurre en
Android, iOS, simulador o dispositivo.

Los informes se evaluarán con la mejor diligencia posible. Este repositorio no promete un plazo
de respuesta ni un programa de recompensas.
