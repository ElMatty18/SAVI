#!/usr/bin/env bash
# Compila la app, levanta el emulador (si no hay un dispositivo conectado) e instala y abre SAVI.
# Uso: scripts/run.sh [--headless]
set -euo pipefail

cd "$(dirname "$0")/.."

SDK="${ANDROID_HOME:-$(sed -n 's/^sdk.dir=//p' local.properties 2>/dev/null)}"
SDK="${SDK:-$HOME/Android/Sdk}"
ADB="$SDK/platform-tools/adb"
EMULATOR="$SDK/emulator/emulator"
AVD="${AVD:-savi}"

if [ ! -x "$ADB" ]; then
  echo "No se encontro el Android SDK en $SDK (ver README: 'Entorno local')." >&2
  exit 1
fi

if ! "$ADB" devices | grep -qE '\s(device)$'; then
  echo "> Iniciando emulador '$AVD'..."
  args=(-avd "$AVD" -no-snapshot-save)
  [ "${1:-}" = "--headless" ] && args+=(-no-window -no-audio)
  nohup "$EMULATOR" "${args[@]}" >/tmp/savi-emulator.log 2>&1 &
  "$ADB" wait-for-device
  until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 2; done
fi

echo "> Compilando e instalando..."
./gradlew installDebug --console=plain -q

echo "> Abriendo SAVI"
"$ADB" shell am start -n com.tangorra.matias.savi/.Activitys.MainActivity >/dev/null
