# CreditPhone Android

App que se instala en el equipo del cliente. Se registra contra el backend, pide permisos de
administrador del dispositivo, y escucha comandos de bloqueo/desbloqueo enviados por Firebase Cloud
Messaging.

## Antes de compilar: falta `google-services.json`

Este archivo es obligatorio y no está incluido (se genera por proyecto). Pasos:

1. En Firebase Console → tu proyecto → ícono de engranaje → **Configuración del proyecto** → pestaña **General**
2. Bajá hasta "Tus apps" → **"Agregar app"** → elegí el ícono de Android
3. En "Nombre del paquete de Android" escribí exactamente: `com.creditphone.app`
4. Seguí los pasos, descargá el archivo `google-services.json`
5. Colocalo dentro de la carpeta `app/` de este proyecto (al lado de `build.gradle`)

Sin este archivo, la compilación en GitHub Actions va a fallar.

## Cómo se compila

No hace falta Android Studio instalado en la Chromebook. Cada vez que subís cambios (`git push`),
GitHub Actions compila automáticamente un APK de prueba (`.github/workflows/build.yml`) y lo deja
disponible para descargar en la pestaña **Actions** del repositorio, dentro de "Artifacts".

## Cómo se usa (primera vez en un equipo)

1. Instalar el APK en el teléfono (`Ajustes > Seguridad > permitir instalación de fuentes desconocidas`
   si hace falta)
2. Abrir la app → copiar el **ID del equipo (deviceUid)** que muestra en pantalla
3. En el panel administrativo, crear el contrato del cliente usando ese mismo `deviceUid`
4. Volver a la app → escribir la dirección del backend (IP local de la Chromebook + puerto 4000) →
   **Guardar dirección**
5. Tocar **"1. Activar administrador de dispositivo"** y aceptar los permisos
6. Tocar **"2. Registrar este equipo"**

Desde ese momento, cuando el backend detecte mora (o se bloquee manualmente desde el panel), el
teléfono va a recibir el comando y bloquear la pantalla automáticamente.

## Importante: esta es la versión "Device Admin", no "Device Owner"

Esta primera versión usa permisos de **administrador de dispositivo** (Device Admin), que son fáciles
de otorgar en cualquier teléfono ya en uso, sin resetear nada. Sirve perfecto para probar que todo
el flujo funciona de punta a punta.

Sin embargo, un usuario que conozca su propio PIN puede desactivar estos permisos manualmente desde
Ajustes y desinstalar la app. Para un bloqueo que no se pueda quitar sin autorización (como Nuovo
Play), el siguiente paso es convertir la app en **Device Owner**, lo que requiere configurarla en un
equipo recién restaurado de fábrica (sin cuentas de Google agregadas) usando un comando ADB. Eso lo
vemos en la siguiente etapa, una vez que confirmemos que el flujo básico funciona.
