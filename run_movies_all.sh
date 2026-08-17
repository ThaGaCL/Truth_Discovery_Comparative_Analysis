#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$ROOT_DIR/.lib"
MATH3_JAR="$LIB_DIR/commons-math3-3.6.1.jar"
MATH3_URL="https://repo1.maven.org/maven2/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar"
OUT_CSV="$ROOT_DIR/DAFNAData/experiments/voterLog/movies_all_algorithms.csv"

mkdir -p "$LIB_DIR"
mkdir -p "$(dirname "$OUT_CSV")"

ensure_math3() {
  if [[ -f "$MATH3_JAR" ]]; then
    return 0
  fi

  echo "[INFO] commons-math3 não encontrado localmente. Baixando para .lib/..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$MATH3_URL" -o "$MATH3_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$MATH3_JAR" "$MATH3_URL"
  else
    echo "[WARN] Nem curl nem wget disponíveis. Execução seguirá sem Math3 (algoritmos dependentes serão SKIPPED)."
    return 1
  fi

  echo "[OK] Jar baixado: $MATH3_JAR"
  return 0
}

compile_runner() {
  echo "[INFO] Compilando classes necessárias..."
  javac --release 25 -cp "$ROOT_DIR/source/bin${EXTRA_CP:+:$EXTRA_CP}" -d "$ROOT_DIR/build/classes" \
    "$ROOT_DIR/source/java-code/qcri/dafna/dataModel/dataFormatter/DataCleaner.java" \
    "$ROOT_DIR/source/java-code/qcri/dafna/dataModel/dataFormatter/DataComparator.java" \
    "$ROOT_DIR/source/java-code/main/MoviesAllAlgorithmsRunner.java"
}

run_runner() {
  echo "[INFO] Processando dataset movies..."
  java -cp "$ROOT_DIR/build/classes" main.MainClass movies >/dev/null

  echo "[INFO] Executando todos os algoritmos para movies..."
  
    java -cp "$ROOT_DIR/build/classes:$ROOT_DIR/source/bin${EXTRA_CP:+:$EXTRA_CP}" main.MoviesAllAlgorithmsRunner | tee "$OUT_CSV"

  echo ""
  echo "[OK] Resultado salvo em: $OUT_CSV"
  echo "[OK] Finalizado."
}

EXTRA_CP=""
if ensure_math3; then
  EXTRA_CP="$MATH3_JAR"
fi

compile_runner
run_runner
