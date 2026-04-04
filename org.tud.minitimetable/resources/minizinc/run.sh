#!/bin/bash

modelFile="./model/v3/AllConstraints.mzn"
gurobiParam="./model/Gurobi.prm"
miniZincDir="/opt/minizinc/MiniZincIDE-2.9.5-bundle-linux-x86_64"


for i in {01..02};do
	dataFile="./data/i$i.json"
	outDir="./out/i$i-01/"
	
	mkdir -p "$outDir"
	
    ARGS=(
        "-m" "$modelFile"
        "-e" "$miniZincDir"
        "-gP" "$gurobiParam"
        "-d" "$dataFile"
        "-o" "$outDir"
    )
	
	echo ">>> Start solving: $dataFile"
	
	echo java -jar ./MiniZincRunner.jar "${ARGS[@]}" -O1 -p 4 -tm 2 -stm 2 &> "$outDir/run.log" || {
        echo "ERROR: $dataFile!"
        exit 1
    }
done

echo "Done"