#!/usr/bin/env bash
# Builds the Telegram Mini App (web) version from the shared Kotlin code.
# Usage: KOTLINC=path/to/kotlinc-js KSTD=path/to/kotlin-stdlib-js.klib VERSION=abc bash web/build.sh
set -euo pipefail
KOTLINC=${KOTLINC:-kotlinc/bin/kotlinc-js}
KSTD=${KSTD:-kotlinc/lib/kotlin-stdlib-js.klib}
VERSION=${VERSION:-$(date +%s)}
OUT="$(pwd)/build/web"
rm -rf "$OUT" && mkdir -p "$OUT/klib" "$OUT/js" "$OUT/site"
"$KOTLINC" -libraries "$KSTD" -Xir-produce-klib-dir -ir-output-dir "$OUT/klib" -ir-output-name botir shared/src web/src
"$KOTLINC" -libraries "$KSTD" -Xir-produce-js -Xinclude="$OUT/klib" -ir-output-dir "$OUT/js" -ir-output-name botir -module-kind plain -main call
npx -y terser@5.31.6 "$OUT/js/botir.js" -c -m -o "$OUT/site/botir.js"
cp web/static/* "$OUT/site/"
# cache-busting so Telegram picks up new versions right away
sed -i "s#botir.js\"#botir.js?v=${VERSION}\"#" "$OUT/site/index.html"
touch "$OUT/site/.nojekyll"
ls -la "$OUT/site"
