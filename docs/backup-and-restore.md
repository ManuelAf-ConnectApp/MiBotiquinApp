# Copia y restauración local

MiBotiquinApp exporta únicamente los datos de salud de la cuenta local activa: tratamientos,
tomas, inventario y movimientos. Nunca incluye la contraseña de acceso, el código de
recuperación ni la sesión.

La copia usa una contraseña independiente, PBKDF2-HMAC-SHA-256 con 600.000 iteraciones y
AES-GCM autenticado. La sal y los parámetros se guardan en el sobre cifrado; una contraseña
incorrecta o un archivo modificado se rechazan. La contraseña no se puede recuperar.

La restauración solo se permite en una cuenta activa sin tratamientos, tomas, cuentas de stock ni
movimientos. No mezcla ni sustituye datos existentes. Los IDs se regeneran para evitar
colisiones con otras cuentas locales, conservando las relaciones entre tratamientos, tomas e
inventario.

## Pruebas manuales por parte de ManuAF

En Android, Perfil permite crear y abrir el archivo mediante el selector del sistema. Verifica
una exportación y restauración con una cuenta vacía, una contraseña errónea, un archivo alterado
y una cuenta que ya tenga datos. No compartas copias reales: contienen datos de salud aunque
estén cifradas.

El selector iOS requiere aceptación en un dispositivo firmado. El cifrado y el formato común
compilan para iOS.
