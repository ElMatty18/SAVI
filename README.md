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

- `database.rules.json` son las reglas de la Realtime Database: exigen autenticación, bloquean el campo `clave` y declaran índices.
- `storage.rules` restringe las fotos de perfil a usuarios autenticados, imágenes de hasta 5 MB.
- `functions/` contiene las Cloud Functions que mandan un push FCM al crearse una alerta o una notificación, para que lleguen con la app cerrada. Requieren el plan Blaze.

```bash
npm i -g firebase-tools && firebase login
(cd functions && npm ci)
firebase deploy --only database,storage,functions
```

### Cómo llegan las alertas

- **Con la app abierta**, `AlertaService` y `NotificacionService` escuchan la base.
- **Con la app cerrada**, `pushAlerta` y `pushNotificacion` (en `functions/`) mandan un mensaje de datos, y `SaviMessagingService` lo recibe.
- Las dos vías terminan en `ProcesadorAlertas`. Las reglas de cada tipo de alerta viven en `PoliticaAlertas` y `PoliticaNotificaciones`, que tienen tests.

## Pendiente conocido

- **Los ids de `/Usuario` son push-keys, no el `uid` de Firebase Auth.** Por eso las reglas solo pueden exigir `auth != null` y no "cada uno escribe lo suyo". Para cerrarlo hay que migrar las claves a `auth.uid`.
- **Las pantallas acceden a Firebase directamente.** El siguiente paso de arquitectura es una capa de repositorio con ViewModel/LiveData y pasar los textos de `StringUtils` a `strings.xml`.
