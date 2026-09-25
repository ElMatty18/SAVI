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

### Probar sin tocar producción

La variante **`emulador`** se instala como una app aparte, "SAVI emu", y usa los emuladores locales de Firebase con datos de demo. Aplica las mismas reglas de seguridad que producción.

```bash
cd tools && npm ci && npm run emulador   # terminal 1: Auth + Database + Storage, UI en http://localhost:4000
scripts/run.sh --emulador                # terminal 2: instala y abre SAVI emu
```

Usuarios de demo, todos con clave `Demo1234`:

| Usuario | Grupo | Familia |
|---|---|---|
| `ana@demo.com` | Barrio Demo | con Beto |
| `beto@demo.com` | Barrio Demo | con Ana |
| `caro@demo.com` | Barrio Demo | sin familia |
| `dani@demo.com` | sin grupo | sin familia |

Los datos se reinician cada vez que se levanta el emulador.

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

## Estructura

```
app/src/main/java/com/tangorra/matias/savi/
  ui/            Pantallas, una carpeta por area, cada una con su ViewModel
    acceso/        login y registro (splash incluido)
    inicio/        pantalla principal y menu lateral
    alertas/       emitir, detalle en vivo e historial
    familia/  grupo/  notificaciones/  perfil/  configuracion/  informacion/
    comun/         piezas compartidas: lista de alertas, selector de ubicacion, dialogo QR
  data/          Repositorios de Firebase, FirebaseLiveData y Sesion (usuario en vivo)
  Service/       Servicios de escucha, FCM y reglas de negocio (PoliticaAlertas, ...)
  Entidades/     Modelos guardados en la base
  Utils/         Validaciones, fechas, notificaciones del sistema
```

- **Firebase se usa solo a través de los repositorios**, obtenidos con `FirebaseUtils.db()` y `storage()`. `FirebaseDatabase.getInstance()` puede devolver una instancia sin la configuración del emulador.
- **Las reglas de negocio no dependen de Android** y tienen tests: `PoliticaAlertas`, `PoliticaNotificaciones`, `Configuracion.vigente()` y `Validaciones`.
- **Tema Material 3** (`Theme.Savi`) con modo claro y oscuro; los textos de interfaz están en `res/values/strings_ui.xml`.

## Pendiente conocido

- **Cualquier usuario autenticado puede leer los perfiles** (incluidos DNI y teléfonos), porque el grupo y la familia los necesitan. Una mejora posible es separar los datos sensibles en un nodo privado.
- **`StringUtils` todavía tiene los tipos de alerta y los estados.** Son valores que se guardan en la base (por ejemplo `"Sospecha de robo"`), así que no pueden pasar a recursos traducibles sin migrar los datos.
- **`SesionManager` (estático) convive con `Sesion`**, que lo mantiene sincronizado. Se puede eliminar migrando sus usos a `Sesion.usuario()`.

## Licencia

[MIT](LICENSE) © 2019-2026 Jesús Matías Ezequiel Tangorra.

Los íconos de interfaz (`ic_*.xml`) son [Material Symbols](https://fonts.google.com/icons) de Google, con licencia Apache 2.0.
