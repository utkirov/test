#!/usr/bin/env bash
# Runs inside the Android emulator job: installs the debug build, runs the in-app self-test,
# renders store screenshots and does a plain launch smoke test. Never fails the job by itself:
# results are written to test-out/ and published with the release.
set -x
PKG=uz.botir.game
OUT=test-out
mkdir -p "$OUT/promo"

adb wait-for-device
adb install -r out/Botir-debug.apk 2>&1 | tee "$OUT/install.txt"

wait_file () {   # $1 = path inside app files, $2 = seconds
  for i in $(seq 1 $(( $2 / 5 ))); do
    if adb shell run-as $PKG ls "files/$1" >/dev/null 2>&1; then return 0; fi
    sleep 5
  done
  return 1
}

# 1) self-test: a bot plays every level, endless mode and menus
adb logcat -c
adb shell am start -n $PKG/.MainActivity --es mode selftest
wait_file selftest.txt 900 || echo "selftest timed out" > "$OUT/selftest.txt"
adb exec-out run-as $PKG cat files/selftest.txt > "$OUT/selftest.txt" 2>/dev/null || true
adb logcat -d > "$OUT/logcat_selftest.txt"
adb shell am force-stop $PKG

# 2) store screenshots, icon and feature graphic rendered by the game itself
adb shell am start -n $PKG/.MainActivity --es mode promo
wait_file promo/done.txt 300 || echo "promo timed out"
for f in $(adb shell run-as $PKG ls files/promo | tr -d '\r'); do
  adb exec-out run-as $PKG cat "files/promo/$f" > "$OUT/promo/$f"
done
adb shell am force-stop $PKG

# 3) normal launch, real screen capture, tap through to gameplay
adb logcat -c
adb shell am start -n $PKG/.MainActivity
sleep 15
adb exec-out screencap -p > "$OUT/device_title.png"
SIZE=$(adb shell wm size | tr -d '\r' | awk '{print $3}')
W=${SIZE%x*}; H=${SIZE#*x}
adb shell am force-stop $PKG
adb shell am start -n $PKG/.MainActivity --es mode play
sleep 8
adb shell input swipe $((W*30/100)) $((H*88/100)) $((W*30/100)) $((H*88/100)) 2500
sleep 1
adb exec-out screencap -p > "$OUT/device_play.png"
adb logcat -d > "$OUT/logcat_run.txt"
grep -hE "FATAL EXCEPTION|AndroidRuntime: |Process: $PKG" "$OUT"/logcat_*.txt > "$OUT/crashes.txt" || echo "no crashes" > "$OUT/crashes.txt"
cat "$OUT/selftest.txt"
cat "$OUT/crashes.txt"
exit 0
