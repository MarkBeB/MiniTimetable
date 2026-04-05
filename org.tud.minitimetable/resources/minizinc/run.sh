#!/bin/bash

miniZincDir="/opt/minizinc/MiniZincIDE-2.9.5-bundle-linux-x86_64"
modelFile="./model/v3/AllConstraints.mzn"

OUTPUT_DIR="./out"

while getopts "o:" opt; do
  case ${opt} in
    o)
      OUTPUT_DIR=$OPTARG
      ;;
  esac
done



#for i in $(seq 1 $#); do
  # Wir prüfen jedes Argument einzeln
#  arg="${!i}"   # Das ist "Indirect Expansion" um auf $1, $2 etc. zuzugreifen
  
#  if [[ "$arg" == "-o" || "$arg" == "--out" ]]; then
    # Das nächste Argument nach der Flag ist der Ordner
#    next_index=$((i + 1))
#    OUTPUT_DIR="${!next_index}"
#  fi
#done

mkdir -p "$OUTPUT_DIR"


java -jar ./MiniZincRunner.jar -e "$miniZincDir" -m "$modelFile" "$@" &> "$OUTPUT_DIR/run.log" 