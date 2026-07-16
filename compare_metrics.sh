#!/usr/bin/env bash
set -euo pipefail

# Compare claims vs truth for the local sample datasets.
# Output: TP, FP, FN, precision, recall, F1 and overlap-accuracy (TP/(TP+FP+FN)).

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

count_lines() {
  local file_path="$1"
  if [[ -s "$file_path" ]]; then
    wc -l < "$file_path" | tr -d ' '
  else
    echo 0
  fi
}

safe_div() {
  local num="$1"
  local den="$2"
  awk -v n="$num" -v d="$den" 'BEGIN { if (d == 0) print "0.0000"; else printf "%.4f", n/d }'
}

build_weather_claims() {
  awk -F'[|]' '{
    oid=$3; prop=$4; val=$5;
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", oid);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", prop);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", val);
    print oid"|"prop"|"val;
  }' "$ROOT_DIR/DAFNAData/formatted/weather/claims/weather-accuweather.txt" | sort -u
}

build_weather_truth() {
  awk -F'[|]' '{
    oid=$3; prop=$4; val=$5;
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", oid);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", prop);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", val);
    print oid"|"prop"|"val;
  }' "$ROOT_DIR/DAFNAData/formatted/weather/truth/weather-truth.txt" | sort -u
}

build_flight_claims() {
  awk -F'[|]' '{
    oid=$3; prop=$4; val=$5;
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", oid);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", prop);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", val);
    print oid"|"prop"|"val;
  }' "$ROOT_DIR/DAFNAData/formatted/flights/claims/2013-12-02-Flight-1.txt" | sort -u
}

build_flight_truth() {
  awk -F'\t' '{
    oid=$1;
    print oid"|ExpectedDepartureTime|"$2;
    print oid"|ActualDepartureTime|"$3;
    print oid"|DepartureGate|"$4;
    print oid"|ExpectedArrivalTime|"$5;
    print oid"|ActualArrivalTime|"$6;
    if (NF>=7 && $7!="" && $7!="--") print oid"|ArrivalGate|"$7;
  }' "$ROOT_DIR/DAFNAData/formatted/flights/flight_truth/2013-12-02-truth.txt" | sort -u
}

build_population_claims() {
  awk -F'[|]' '{
    oid=$3; prop=$4; val=$5;
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", oid);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", prop);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", val);
    print oid"|"prop"|"val;
  }' "$ROOT_DIR/DAFNAData/formatted/Population/claims/population-1.txt" | sort -u
}

build_population_truth() {
  awk -F',' '{
    city=$1", "$2;
    year=$3;
    val=$4;
    gsub(/^ +| +$/, "", city);
    gsub(/, +/, ", ", city);
    gsub(/^ +| +$/, "", year);
    gsub(/^ +| +$/, "", val);
    print city"|Population"year"|"val;
  }' "$ROOT_DIR/DAFNAData/formatted/Population/truth/population-truth.txt" | sort -u
}

build_bio_claims() {
  awk -F'[|]' '{
    oid=$3; prop=$4; val=$5;
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", oid);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", prop);
    gsub(/^[[:space:]]+|[[:space:]]+$/, "", val);
    print oid"|"prop"|"val;
  }' "$ROOT_DIR/DAFNAData/formatted/Biographies/claims/bio-1.txt" | sort -u
}

build_bio_truth() {
  awk -F'\t' '{print $1"|"$2"|"$3}' "$ROOT_DIR/DAFNAData/formatted/Biographies/truth/biography-truth.txt" | sort -u
}

compute_metrics() {
  local label="$1"
  local claims_file="$2"
  local truth_file="$3"

  comm -12 "$claims_file" "$truth_file" > "$TMP_DIR/tp.$label"
  comm -23 "$claims_file" "$truth_file" > "$TMP_DIR/fp.$label"
  comm -13 "$claims_file" "$truth_file" > "$TMP_DIR/fn.$label"

  local tp fp fn
  tp="$(count_lines "$TMP_DIR/tp.$label")"
  fp="$(count_lines "$TMP_DIR/fp.$label")"
  fn="$(count_lines "$TMP_DIR/fn.$label")"

  local precision recall f1 overlap_accuracy
  precision="$(safe_div "$tp" "$((tp + fp))")"
  recall="$(safe_div "$tp" "$((tp + fn))")"
  f1="$(awk -v p="$precision" -v r="$recall" 'BEGIN { if ((p+r) == 0) print "0.0000"; else printf "%.4f", (2*p*r)/(p+r) }')"
  overlap_accuracy="$(safe_div "$tp" "$((tp + fp + fn))")"

  echo "[$label]"
  echo "TP=$tp"
  echo "FP=$fp"
  echo "FN=$fn"
  echo "TN=N/A"
  echo "precision=$precision"
  echo "recall=$recall"
  echo "f1=$f1"
  echo "accuracy_overlap=$overlap_accuracy"
  echo "specificity=N/A"
  echo
}

build_weather_claims > "$TMP_DIR/weather.claims"
build_weather_truth > "$TMP_DIR/weather.truth"
build_flight_claims > "$TMP_DIR/flight.claims"
build_flight_truth > "$TMP_DIR/flight.truth"
build_population_claims > "$TMP_DIR/pop.claims"
build_population_truth > "$TMP_DIR/pop.truth"
build_bio_claims > "$TMP_DIR/bio.claims"
build_bio_truth > "$TMP_DIR/bio.truth"

compute_metrics "weather" "$TMP_DIR/weather.claims" "$TMP_DIR/weather.truth"
compute_metrics "flight" "$TMP_DIR/flight.claims" "$TMP_DIR/flight.truth"
compute_metrics "population" "$TMP_DIR/pop.claims" "$TMP_DIR/pop.truth"
compute_metrics "biography" "$TMP_DIR/bio.claims" "$TMP_DIR/bio.truth"
