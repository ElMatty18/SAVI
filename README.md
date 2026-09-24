# SAVI — Sistema de Asistencia Vecinal Integral

App Android para grupos vecinales y familiares: emitir alertas (alarma sonando, sospecha de robo,
principio de fuego, etc.), responderlas, y recibir notificaciones del barrio según la distancia al domicilio.

**Stack:** Java 17 · Android (minSdk 24, targetSdk 35) · AndroidX · Firebase (Auth, Realtime Database,
Storage, Cloud Messaging, Cloud Functions) · Google Maps.

## Entorno local

Requisitos: JDK 17 o superior y el Android SDK (Android Studio lo instala; alcanza con las *command-line tools*).

```bash
# 1. SDK por línea de comandos (si no usás Android Studio)
sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools" \
           "emulator" "system-images;android-35;google_apis;x86_64"
avdmanager create avd -n savi -k "system-images;android-35;google_apis;x86_64" -d pixel_6

# 2. local.properties (no se versiona)
cat > local.properties <<EOF
sdk.dir=/ruta/al/Android/Sdk
MAPS_API_KEY=AIza...
EOF
```

`MAPS_API_KEY` es la key de Google Maps. En CI se toma de la variable de entorno del mismo nombre.

## Compilar y levantar

```bash
./gradlew assembleDebug          # APK en app/build/outputs/apk/debug/
./gradlew testDebugUnitTest      # tests unitarios
scripts/run.sh                   # levanta el emulador "savi", instala y abre la app
scripts/run.sh --headless        # igual, sin ventana
```

También se puede abrir la carpeta en Android Studio y ejecutar la configuración `app`.

## Firebase

La configuración está en `firebase.json`:

- `database.rules.json` son las reglas **definitivas**. Cada usuario escribe solo su registro (`/Usuario/{uid}`), solo los integrantes de un grupo crean alertas, y cada vecino escribe solo su respuesta y su "visto".
- `database.rules.transicion.json` son reglas **provisorias**: solo exigen estar autenticado. Sirven mientras existan cuentas creadas antes de usar el uid como clave.
- `storage.rules` restringe las fotos de perfil a usuarios autenticados, imágenes de hasta 5 MB.
- `functions/` contiene las Cloud Functions que mandan un push FCM al crearse una alerta o una notificación, para que lleguen con la app cerrada. Requieren el plan Blaze.
- `tools/` tiene los tests de reglas y el script de migración. Corren contra el emulador de Firebase con `cd tools && npm ci && npm test`.

### Modelo de datos

```
Usuario/{uid}                       id = uid de Firebase Auth
Grupo/{idGrupo}/alertas/{idAlerta}
    respuestas/{uid}                una por vecino
Familia/{idFamilia}/{uid}: uid
Notificacion/{id}
    vistoPor/{uid}: uid
```

### Pasar al modelo por uid (una sola vez)

```bash
npm i -g firebase-tools && firebase login

# 1. Ya mismo: cerrar la base (hoy es legible sin autenticación)
firebase deploy --only database --config firebase.transicion.json

# 2. Publicar la versión nueva de la app. Es compatible con cuentas viejas y nuevas.

# 3. Migrar los datos. Necesita una service account del proyecto:
#    Consola > Configuración > Cuentas de servicio > Generar nueva clave privada
export GOOGLE_APPLICATION_CREDENTIALS=/ruta/service-account.json
cd tools && npm ci
npm run migrar                # simulación: muestra qué cambiaría y guarda un backup
npm run migrar -- --aplicar   # migra la base y copia las fotos de perfil

# 4. Reglas definitivas, storage y functions
cd .. && firebase deploy --only database,storage,functions
```

La migración es idempotente: se puede volver a correr sin efectos. Deja intactos los usuarios que no tengan cuenta en Auth y avisa cuáles son.

### Cómo llegan las alertas

- **Con la app abierta**, `AlertaService` y `NotificacionService` escuchan la base.
- **Con la app cerrada**, `pushAlerta` y `pushNotificacion` (en `functions/`) mandan un mensaje de datos, y `SaviMessagingService` lo recibe.
- Las dos vías terminan en `ProcesadorAlertas`. Las reglas de cada tipo de alerta viven en `PoliticaAlertas` y `PoliticaNotificaciones`, que tienen tests.

## Pendiente conocido

- **Cualquier usuario autenticado puede leer los perfiles** (incluidos DNI y teléfonos), porque el grupo y la familia los necesitan. Una mejora posible es separar los datos sensibles en un nodo privado.
- **Las pantallas acceden a Firebase directamente.** El siguiente paso de arquitectura es una capa de repositorio con ViewModel/LiveData y pasar los textos de `StringUtils` a `strings.xml`.
