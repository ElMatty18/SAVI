#!/usr/bin/env bash
# Compila la app, levanta el emulador (si no hay un dispositivo conectado) e instala y abre SAVI.
# Uso: scripts/run.sh [--headless] [--emulador]
#   --emulador  instala "SAVI emu", que usa el emulador local de Firebase con datos de demo
#               (antes correr: cd tools && npm run emulador)
set -euo pipefail

cd "$(dirname "$0")/.."

SDK="${ANDROID_HOME:-$(sed -n 's/^sdk.dir=//p' local.properties 2>/dev/null)}"
SDK="${SDK:-$HOME/Android/Sdk}"
ADB="$SDK/platform-tools/adb"
EMULATOR="$SDK/emulator/emulator"
AVD="${AVD:-savi}"
HEADLESS=false
FIREBASE_LOCAL=false
for arg in "$@"; do
  case "$arg" in
    --headless) HEADLESS=true ;;
    --emulador) FIREBASE_LOCAL=true ;;
  esac
done

if [ ! -x "$ADB" ]; then
  echo "No se encontro el Android SDK en $SDK (ver README: 'Entorno local')." >&2
  exit 1
fi

if ! "$ADB" devices | grep -qE '\s(device)$'; then
  echo "> Iniciando emulador '$AVD'..."
  args=(-avd "$AVD" -no-snapshot-save)
  [ "$HEADLESS" = true ] && args+=(-no-window -no-audio)
  # setsid: el emulador sigue vivo aunque se cierre la terminal que lo lanzo
  setsid nohup "$EMULATOR" "${args[@]}" >/tmp/savi-emulator.log 2>&1 < /dev/null &
  "$ADB" wait-for-device
  until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 2; done
fi

if [ "$FIREBASE_LOCAL" = true ]; then
  # El dispositivo accede a los emuladores de Firebase (Database, Auth, Storage) de la PC via 127.0.0.1
  for puerto in 9000 9099 9199; do "$ADB" reverse "tcp:$puerto" "tcp:$puerto" >/dev/null; done
  TAREA=installEmulador
  PAQUETE=com.tangorra.matias.savi.emu
else
  TAREA=installDebug
  PAQUETE=com.tangorra.matias.savi
fi

echo "> Compilando e instalando ($TAREA)..."
./gradlew "$TAREA" --console=plain -q

echo "> Abriendo SAVI"
"$ADB" shell am start -n "$PAQUETE/com.tangorra.matias.savi.Activitys.MainActivity" >/dev/null
